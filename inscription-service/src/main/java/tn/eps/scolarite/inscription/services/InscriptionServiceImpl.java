package tn.eps.scolarite.inscription.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.eps.scolarite.inscription.clients.CoursRestClient;
import tn.eps.scolarite.inscription.clients.EtudiantRestClient;
import tn.eps.scolarite.inscription.clients.PartenairesCache;
import tn.eps.scolarite.inscription.dto.InscriptionRequestDTO;
import tn.eps.scolarite.inscription.dto.InscriptionResponseDTO;
import tn.eps.scolarite.inscription.dto.LigneReleveDTO;
import tn.eps.scolarite.inscription.dto.ReleveNotesDTO;
import tn.eps.scolarite.inscription.entities.Inscription;
import tn.eps.scolarite.inscription.entities.StatutInscription;
import tn.eps.scolarite.inscription.exceptions.*;
import tn.eps.scolarite.inscription.mappers.InscriptionMapper;
import tn.eps.scolarite.inscription.model.Capacite;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.Etudiant;
import tn.eps.scolarite.inscription.model.SourceDonnees;
import tn.eps.scolarite.inscription.repositories.InscriptionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrateur : combine les données locales (inscription_db) avec celles des services
 * ETUDIANT-SERVICE et COURS-SERVICE obtenues via OpenFeign.
 * <p>
 * Lectures : tolérantes aux pannes (fallback -> cache de secours ou objet "INDISPONIBLE",
 * la réponse reste 200 et signale la dégradation dans "serviceStatus" / "warnings").
 * <br>
 * Écritures (POST) : strictes. Si un partenaire est indisponible on ne peut pas vérifier
 * l'existence de l'étudiant / du cours ni la capacité -> 503 plutôt qu'une inscription "à l'aveugle".
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    static final String ETUDIANT_SERVICE = "ETUDIANT-SERVICE";
    static final String COURS_SERVICE = "COURS-SERVICE";

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRestClient etudiantRestClient;
    private final CoursRestClient coursRestClient;
    private final PartenairesCache cache;
    private final InscriptionMapper mapper;

    // ------------------------------------------------------------------ Lectures

    @Override
    @Transactional(readOnly = true)
    public List<InscriptionResponseDTO> lister() {
        return enrichirTout(inscriptionRepository.findAll(Sort.by("id")));
    }

    @Override
    @Transactional(readOnly = true)
    public InscriptionResponseDTO trouverParId(Long id) {
        Inscription inscription = charger(id);
        enrichir(inscription, chargerEtudiant(inscription.getEtudiantId()), chargerCours(inscription.getCoursId()));
        return mapper.toDto(inscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscriptionResponseDTO> parEtudiant(Long etudiantId) {
        return enrichirTout(inscriptionRepository.findByEtudiantIdOrderByAnneeUniversitaireDescIdAsc(etudiantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscriptionResponseDTO> parCours(Long coursId) {
        return enrichirTout(inscriptionRepository.findByCoursIdOrderByIdAsc(coursId));
    }

    // ------------------------------------------------------------------ Écritures

    @Override
    public InscriptionResponseDTO inscrire(InscriptionRequestDTO dto) {
        // 1) L'étudiant existe-t-il ? (404 propagé par le fallback si le service répond "introuvable")
        Etudiant etudiant = etudiantRestClient.findById(dto.etudiantId());
        if (etudiant == null || etudiant.isDegrade()) {
            throw new ServicePartenaireIndisponibleException(ETUDIANT_SERVICE,
                    "ETUDIANT-SERVICE est indisponible : impossible de vérifier l'étudiant " + dto.etudiantId()
                            + ". L'inscription n'est pas enregistrée, réessayez dans quelques instants.");
        }
        cache.memoriser(etudiant);

        // 2) Le cours existe-t-il ?
        Cours cours = coursRestClient.findById(dto.coursId());
        if (cours == null || cours.isDegrade()) {
            throw new ServicePartenaireIndisponibleException(COURS_SERVICE,
                    "COURS-SERVICE est indisponible : impossible de vérifier le cours " + dto.coursId()
                            + ". L'inscription n'est pas enregistrée, réessayez dans quelques instants.");
        }
        cache.memoriser(cours);

        // 3) Pas de doublon (même étudiant, même cours, même année, hors annulations)
        String annee = dto.anneeUniversitaire() != null ? dto.anneeUniversitaire() : anneeUniversitaireCourante(LocalDate.now());
        if (inscriptionRepository.existsByEtudiantIdAndCoursIdAndAnneeUniversitaireAndStatutNot(
                dto.etudiantId(), dto.coursId(), annee, StatutInscription.ANNULEE)) {
            throw new InscriptionEnDoubleException("L'étudiant " + etudiant.getMatricule() + " est déjà inscrit au cours "
                    + cours.getCode() + " pour l'année " + annee);
        }

        // 4) Capacité restante (appel Feign dédié GET /api/cours/{id}/capacite)
        Capacite capacite = coursRestClient.getCapacite(dto.coursId());
        long placesOccupees = inscriptionRepository.countByCoursIdAndAnneeUniversitaireAndStatutNot(
                dto.coursId(), annee, StatutInscription.ANNULEE);
        if (capacite.getCapaciteMax() != null && placesOccupees >= capacite.getCapaciteMax()) {
            throw new CapaciteAtteinteException("Le cours " + cours.getCode() + " est complet ("
                    + placesOccupees + "/" + capacite.getCapaciteMax() + " places occupées en " + annee + ")");
        }

        Inscription inscription = Inscription.builder()
                .etudiantId(dto.etudiantId())
                .coursId(dto.coursId())
                .anneeUniversitaire(annee)
                .dateInscription(LocalDate.now())
                .statut(StatutInscription.EN_ATTENTE)
                .build();
        Inscription enregistree = inscriptionRepository.save(inscription);
        log.info("Inscription créée : id={} etudiant={} cours={} annee={}", enregistree.getId(),
                etudiant.getMatricule(), cours.getCode(), annee);
        enrichir(enregistree, etudiant, cours);
        return mapper.toDto(enregistree);
    }

    @Override
    public InscriptionResponseDTO changerStatut(Long id, StatutInscription nouveauStatut) {
        Inscription inscription = charger(id);
        if (inscription.getStatut() == StatutInscription.ANNULEE && nouveauStatut != StatutInscription.ANNULEE) {
            throw new RegleMetierException("Une inscription annulée ne peut pas être réactivée : "
                    + "créez une nouvelle inscription (la capacité sera revérifiée)");
        }
        log.info("Inscription {} : statut {} -> {}", id, inscription.getStatut(), nouveauStatut);
        inscription.setStatut(nouveauStatut);
        Inscription enregistree = inscriptionRepository.save(inscription);
        enrichir(enregistree, chargerEtudiant(enregistree.getEtudiantId()), chargerCours(enregistree.getCoursId()));
        return mapper.toDto(enregistree);
    }

    @Override
    public InscriptionResponseDTO attribuerNote(Long id, Double note) {
        Inscription inscription = charger(id);
        if (inscription.getStatut() == StatutInscription.ANNULEE) {
            throw new RegleMetierException("Impossible de noter une inscription annulée (id=" + id + ")");
        }
        inscription.setNote(note);
        Inscription enregistree = inscriptionRepository.save(inscription);
        log.info("Inscription {} : note = {}", id, note);
        enrichir(enregistree, chargerEtudiant(enregistree.getEtudiantId()), chargerCours(enregistree.getCoursId()));
        return mapper.toDto(enregistree);
    }

    @Override
    public void supprimer(Long id) {
        inscriptionRepository.delete(charger(id));
        log.info("Inscription supprimée : id={}", id);
    }

    // ------------------------------------------------------------------ Relevé de notes

    @Override
    @Transactional(readOnly = true)
    public ReleveNotesDTO releve(Long etudiantId, String anneeUniversitaire) {
        Etudiant etudiant = chargerEtudiant(etudiantId);
        if (etudiant == null) {
            throw new RessourceDistanteIntrouvableException("Étudiant introuvable (id=" + etudiantId + ")");
        }
        List<Inscription> inscriptions = anneeUniversitaire == null
                ? inscriptionRepository.findByEtudiantIdOrderByAnneeUniversitaireDescIdAsc(etudiantId)
                : inscriptionRepository.findByEtudiantIdAndAnneeUniversitaireOrderByIdAsc(etudiantId, anneeUniversitaire);
        inscriptions = inscriptions.stream().filter(i -> i.getStatut() != StatutInscription.ANNULEE).toList();

        Map<Long, Cours> coursCharges = new HashMap<>();
        for (Inscription i : inscriptions) {
            enrichir(i, etudiant, coursCharges.computeIfAbsent(i.getCoursId(), this::chargerCours));
        }
        List<LigneReleveDTO> lignes = inscriptions.stream().map(mapper::toLigne).toList();

        List<LigneReleveDTO> notees = lignes.stream()
                .filter(l -> l.note() != null && l.coefficient() != null).toList();
        long notesSansCoefficient = lignes.stream().filter(l -> l.note() != null && l.coefficient() == null).count();

        double totalCoef = notees.stream().mapToDouble(LigneReleveDTO::coefficient).sum();
        Double moyenne = totalCoef == 0 ? null
                : arrondir(notees.stream().mapToDouble(l -> l.note() * l.coefficient()).sum() / totalCoef);

        // Crédits ECTS : un module est acquis si la moyenne de ses cours notés est >= 10
        int credits = notees.stream()
                .collect(Collectors.groupingBy(LigneReleveDTO::moduleCode, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .filter(ls -> ls.stream().mapToDouble(LigneReleveDTO::note).average().orElse(0) >= 10)
                .mapToInt(ls -> Optional.ofNullable(ls.get(0).credits()).orElse(0))
                .sum();

        LinkedHashSet<String> warnings = new LinkedHashSet<>();
        inscriptions.forEach(i -> warnings.addAll(i.getWarnings()));
        if (inscriptions.isEmpty()) {
            avertirEtudiant(etudiant, etudiantId, warnings);
        }
        if (notesSansCoefficient > 0) {
            warnings.add(notesSansCoefficient + " note(s) exclue(s) de la moyenne : coefficient inconnu "
                    + "(COURS-SERVICE indisponible) - moyenne partielle");
        }
        return new ReleveNotesDTO(etudiant, anneeUniversitaire, lignes, moyenne, arrondir(totalCoef),
                notees.size(), credits, mention(moyenne), new ArrayList<>(warnings));
    }

    // ------------------------------------------------------------------ Agrégation (Feign)

    /** Enrichit une liste en n'appelant qu'une fois chaque étudiant / cours distinct. */
    private List<InscriptionResponseDTO> enrichirTout(List<Inscription> inscriptions) {
        Map<Long, Optional<Etudiant>> etudiants = new HashMap<>();
        Map<Long, Optional<Cours>> cours = new HashMap<>();
        return inscriptions.stream().map(i -> {
            Etudiant e = etudiants.computeIfAbsent(i.getEtudiantId(), id -> Optional.ofNullable(chargerEtudiant(id))).orElse(null);
            Cours c = cours.computeIfAbsent(i.getCoursId(), id -> Optional.ofNullable(chargerCours(id))).orElse(null);
            enrichir(i, e, c);
            return mapper.toDto(i);
        }).toList();
    }

    /** Remplit les champs @Transient et calcule les avertissements de dégradation. */
    private void enrichir(Inscription i, Etudiant etudiant, Cours cours) {
        i.setEtudiant(etudiant);
        i.setCours(cours);
        List<String> warnings = new ArrayList<>();
        avertirEtudiant(etudiant, i.getEtudiantId(), warnings);
        if (cours == null) {
            warnings.add("Cours id=" + i.getCoursId() + " introuvable dans COURS-SERVICE (supprimé ?)");
        } else if (cours.getSourceDonnees() == SourceDonnees.CACHE_SECOURS) {
            warnings.add("COURS-SERVICE indisponible : données du cours issues du cache de secours");
        } else if (cours.getSourceDonnees() == SourceDonnees.INDISPONIBLE) {
            warnings.add("COURS-SERVICE indisponible : données du cours non disponibles");
        }
        i.setWarnings(warnings);
    }

    private static void avertirEtudiant(Etudiant etudiant, Long etudiantId, Collection<String> warnings) {
        if (etudiant == null) {
            warnings.add("Étudiant id=" + etudiantId + " introuvable dans ETUDIANT-SERVICE (supprimé ?)");
        } else if (etudiant.getSourceDonnees() == SourceDonnees.CACHE_SECOURS) {
            warnings.add("ETUDIANT-SERVICE indisponible : données étudiant issues du cache de secours");
        } else if (etudiant.getSourceDonnees() == SourceDonnees.INDISPONIBLE) {
            warnings.add("ETUDIANT-SERVICE indisponible : données étudiant non disponibles");
        }
    }

    /** Appel Feign protégé : renvoie l'étudiant (éventuellement dégradé) ou null s'il n'existe pas. */
    private Etudiant chargerEtudiant(Long id) {
        try {
            Etudiant e = etudiantRestClient.findById(id);
            cache.memoriser(e);
            return e;
        } catch (RessourceDistanteIntrouvableException ex) {
            cache.oublierEtudiant(id);
            return null;
        }
    }

    private Cours chargerCours(Long id) {
        try {
            Cours c = coursRestClient.findById(id);
            cache.memoriser(c);
            return c;
        } catch (RessourceDistanteIntrouvableException ex) {
            cache.oublierCours(id);
            return null;
        }
    }

    private Inscription charger(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription introuvable (id=" + id + ")"));
    }

    // ------------------------------------------------------------------ Utilitaires

    /** Septembre -> août : en novembre 2026 l'année universitaire est "2026-2027". */
    static String anneeUniversitaireCourante(LocalDate date) {
        int debut = date.getMonthValue() >= 9 ? date.getYear() : date.getYear() - 1;
        return debut + "-" + (debut + 1);
    }

    static String mention(Double moyenne) {
        if (moyenne == null) return "Non évalué";
        if (moyenne >= 16) return "Très bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez bien";
        if (moyenne >= 10) return "Passable";
        return "Ajourné";
    }

    private static double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
