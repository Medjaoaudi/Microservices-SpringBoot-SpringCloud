package tn.eps.scolarite.inscription.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.eps.scolarite.inscription.clients.CoursRestClient;
import tn.eps.scolarite.inscription.clients.EtudiantRestClient;
import tn.eps.scolarite.inscription.clients.PartenairesCache;
import tn.eps.scolarite.inscription.dto.InscriptionRequestDTO;
import tn.eps.scolarite.inscription.dto.InscriptionResponseDTO;
import tn.eps.scolarite.inscription.dto.ReleveNotesDTO;
import tn.eps.scolarite.inscription.entities.Inscription;
import tn.eps.scolarite.inscription.entities.StatutInscription;
import tn.eps.scolarite.inscription.exceptions.*;
import tn.eps.scolarite.inscription.mappers.InscriptionMapper;
import tn.eps.scolarite.inscription.model.*;
import tn.eps.scolarite.inscription.repositories.InscriptionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscriptionServiceImplTest {

    private static final String ANNEE = "2026-2027";

    @Mock
    private InscriptionRepository repository;
    @Mock
    private EtudiantRestClient etudiantClient;
    @Mock
    private CoursRestClient coursClient;

    private PartenairesCache cache;
    private InscriptionServiceImpl service;

    private final Etudiant amine = Etudiant.builder().id(1L).matricule("ETU-2026-001")
            .nom("Ben Salah").prenom("Amine").build();
    private final ModuleInfo ueJee = ModuleInfo.builder().id(1L).code("UE-JEE2").coefficient(3.0).credits(5).build();
    private final ModuleInfo ueArchi = ModuleInfo.builder().id(2L).code("UE-ARCHI").coefficient(2.5).credits(4).build();
    private final Cours jeeC = Cours.builder().id(1L).code("JEE2-C").titre("Microservices")
            .capaciteMax(40).module(ueJee).build();
    private final Cours archiC = Cours.builder().id(3L).code("ARCHI-C").titre("Patterns")
            .capaciteMax(40).module(ueArchi).build();

    @BeforeEach
    void setUp() {
        cache = new PartenairesCache();
        service = new InscriptionServiceImpl(repository, etudiantClient, coursClient, cache, new InscriptionMapper());
    }

    private static Inscription inscription(long id, long etudiantId, long coursId, StatutInscription statut, Double note) {
        return Inscription.builder().id(id).etudiantId(etudiantId).coursId(coursId)
                .anneeUniversitaire(ANNEE).dateInscription(LocalDate.of(2026, 9, 15))
                .statut(statut).note(note).build();
    }

    @Nested
    @DisplayName("POST /api/inscriptions (création orchestrée)")
    class Creation {

        private final InscriptionRequestDTO demande = new InscriptionRequestDTO(1L, 1L, ANNEE);

        @Test
        @DisplayName("Cas nominal : étudiant et cours vérifiés via Feign, inscription EN_ATTENTE enrichie")
        void nominal() {
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(coursClient.findById(1L)).thenReturn(jeeC);
            when(coursClient.getCapacite(1L)).thenReturn(new Capacite(1L, "JEE2-C", 40));
            when(repository.existsByEtudiantIdAndCoursIdAndAnneeUniversitaireAndStatutNot(1L, 1L, ANNEE,
                    StatutInscription.ANNULEE)).thenReturn(false);
            when(repository.countByCoursIdAndAnneeUniversitaireAndStatutNot(1L, ANNEE, StatutInscription.ANNULEE))
                    .thenReturn(10L);
            when(repository.save(any(Inscription.class))).thenAnswer(inv -> {
                Inscription i = inv.getArgument(0);
                i.setId(100L);
                return i;
            });

            InscriptionResponseDTO dto = service.inscrire(demande);

            assertThat(dto.id()).isEqualTo(100L);
            assertThat(dto.statut()).isEqualTo(StatutInscription.EN_ATTENTE);
            assertThat(dto.etudiant().getMatricule()).isEqualTo("ETU-2026-001");
            assertThat(dto.cours().getCode()).isEqualTo("JEE2-C");
            assertThat(dto.serviceStatus()).containsEntry("ETUDIANT-SERVICE", "UP").containsEntry("COURS-SERVICE", "UP");
            assertThat(dto.warnings()).isEmpty();
            // Les données fraîches alimentent le cache de secours
            assertThat(cache.etudiant(1L)).isPresent();
        }

        @Test
        @DisplayName("Étudiant introuvable : 404 propagé, rien n'est enregistré")
        void etudiantIntrouvable() {
            when(etudiantClient.findById(1L)).thenThrow(new RessourceDistanteIntrouvableException("Étudiant introuvable (id=1)"));

            assertThatThrownBy(() -> service.inscrire(demande)).isInstanceOf(RessourceDistanteIntrouvableException.class);
            verifyNoInteractions(coursClient);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Capacité atteinte : 409")
        void capaciteAtteinte() {
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(coursClient.findById(1L)).thenReturn(jeeC);
            when(coursClient.getCapacite(1L)).thenReturn(new Capacite(1L, "JEE2-C", 3));
            when(repository.countByCoursIdAndAnneeUniversitaireAndStatutNot(1L, ANNEE, StatutInscription.ANNULEE))
                    .thenReturn(3L);

            assertThatThrownBy(() -> service.inscrire(demande))
                    .isInstanceOf(CapaciteAtteinteException.class)
                    .hasMessageContaining("3/3");
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Doublon (même étudiant, même cours, même année) : 409")
        void doublon() {
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(coursClient.findById(1L)).thenReturn(jeeC);
            when(repository.existsByEtudiantIdAndCoursIdAndAnneeUniversitaireAndStatutNot(1L, 1L, ANNEE,
                    StatutInscription.ANNULEE)).thenReturn(true);

            assertThatThrownBy(() -> service.inscrire(demande)).isInstanceOf(InscriptionEnDoubleException.class);
            verify(coursClient, never()).getCapacite(anyLong());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Fallback Feign (ETUDIANT-SERVICE en panne) : 503, pas d'inscription à l'aveugle")
        void fallbackEtudiant_503() {
            // Ce que renvoie la FallbackFactory quand le service est hors ligne
            when(etudiantClient.findById(1L)).thenReturn(Etudiant.indisponible(1L));

            assertThatThrownBy(() -> service.inscrire(demande))
                    .isInstanceOf(ServicePartenaireIndisponibleException.class)
                    .hasMessageContaining("ETUDIANT-SERVICE");
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Fallback Feign avec cache (données CACHE_SECOURS) : 503 également")
        void fallbackCoursDepuisCache_503() {
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(coursClient.findById(1L)).thenReturn(jeeC.toBuilder().sourceDonnees(SourceDonnees.CACHE_SECOURS).build());

            assertThatThrownBy(() -> service.inscrire(demande))
                    .isInstanceOf(ServicePartenaireIndisponibleException.class)
                    .hasMessageContaining("COURS-SERVICE");
        }

        @Test
        @DisplayName("Année universitaire par défaut : septembre -> août")
        void anneeParDefaut() {
            assertThat(InscriptionServiceImpl.anneeUniversitaireCourante(LocalDate.of(2026, 11, 17))).isEqualTo("2026-2027");
            assertThat(InscriptionServiceImpl.anneeUniversitaireCourante(LocalDate.of(2027, 3, 1))).isEqualTo("2026-2027");
        }
    }

    @Nested
    @DisplayName("Lectures enrichies (GET)")
    class Lectures {

        @Test
        @DisplayName("Champs @Transient remplis quand les partenaires répondent")
        void enrichie() {
            when(repository.findById(1L)).thenReturn(Optional.of(inscription(1, 1, 1, StatutInscription.VALIDEE, 16.5)));
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(coursClient.findById(1L)).thenReturn(jeeC);

            InscriptionResponseDTO dto = service.trouverParId(1L);

            assertThat(dto.etudiant().getNom()).isEqualTo("Ben Salah");
            assertThat(dto.cours().getModule().getCode()).isEqualTo("UE-JEE2");
            assertThat(dto.warnings()).isEmpty();
        }

        @Test
        @DisplayName("Fallback Feign : réponse dégradée (pas d'exception) + warnings + serviceStatus")
        void degradee() {
            when(repository.findById(1L)).thenReturn(Optional.of(inscription(1, 1, 1, StatutInscription.VALIDEE, 16.5)));
            when(etudiantClient.findById(1L)).thenReturn(Etudiant.indisponible(1L));
            when(coursClient.findById(1L)).thenReturn(jeeC);

            InscriptionResponseDTO dto = service.trouverParId(1L);

            assertThat(dto.note()).isEqualTo(16.5);               // données locales intactes
            assertThat(dto.etudiant().getNom()).isEqualTo("INDISPONIBLE");
            assertThat(dto.serviceStatus()).containsEntry("ETUDIANT-SERVICE", "INDISPONIBLE")
                    .containsEntry("COURS-SERVICE", "UP");
            assertThat(dto.warnings()).singleElement().asString().contains("ETUDIANT-SERVICE indisponible");
        }

        @Test
        @DisplayName("Liste : un seul appel Feign par étudiant / cours distinct")
        void listeSansAppelsRedondants() {
            when(repository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(
                    inscription(1, 1, 1, StatutInscription.VALIDEE, 16.5),
                    inscription(2, 1, 3, StatutInscription.VALIDEE, 14.0)));
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(coursClient.findById(1L)).thenReturn(jeeC);
            when(coursClient.findById(3L)).thenReturn(archiC);

            assertThat(service.lister()).hasSize(2);
            verify(etudiantClient, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Inscription inexistante : 404")
        void introuvable() {
            when(repository.findById(9L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.trouverParId(9L)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Relevé de notes et règles de gestion")
    class Releve {

        @Test
        @DisplayName("Moyenne pondérée par les coefficients des modules, annulations exclues")
        void moyennePonderee() {
            when(etudiantClient.findById(1L)).thenReturn(amine);
            when(repository.findByEtudiantIdOrderByAnneeUniversitaireDescIdAsc(1L)).thenReturn(List.of(
                    inscription(1, 1, 1, StatutInscription.VALIDEE, 16.5),
                    inscription(2, 1, 3, StatutInscription.VALIDEE, 14.0),
                    inscription(3, 1, 3, StatutInscription.ANNULEE, 2.0)));
            when(coursClient.findById(1L)).thenReturn(jeeC);
            when(coursClient.findById(3L)).thenReturn(archiC);

            ReleveNotesDTO releve = service.releve(1L, null);

            // (16.5 x 3 + 14 x 2.5) / 5.5 = 84.5 / 5.5 = 15.36
            assertThat(releve.moyennePonderee()).isEqualTo(15.36);
            assertThat(releve.lignes()).hasSize(2);
            assertThat(releve.creditsAcquis()).isEqualTo(9);
            assertThat(releve.mention()).isEqualTo("Bien");
        }

        @Test
        @DisplayName("Impossible de noter une inscription annulée : 409")
        void noteSurAnnulee() {
            when(repository.findById(5L)).thenReturn(Optional.of(inscription(5, 1, 1, StatutInscription.ANNULEE, null)));
            assertThatThrownBy(() -> service.attribuerNote(5L, 12.0)).isInstanceOf(RegleMetierException.class);
        }

        @Test
        @DisplayName("Une inscription annulée ne peut pas être réactivée : 409")
        void reactivationInterdite() {
            when(repository.findById(5L)).thenReturn(Optional.of(inscription(5, 1, 1, StatutInscription.ANNULEE, null)));
            assertThatThrownBy(() -> service.changerStatut(5L, StatutInscription.VALIDEE))
                    .isInstanceOf(RegleMetierException.class);
        }
    }
}
