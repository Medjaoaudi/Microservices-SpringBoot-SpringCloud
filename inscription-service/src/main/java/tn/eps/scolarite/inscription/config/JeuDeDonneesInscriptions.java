package tn.eps.scolarite.inscription.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.eps.scolarite.inscription.entities.Inscription;
import tn.eps.scolarite.inscription.entities.StatutInscription;
import tn.eps.scolarite.inscription.repositories.InscriptionRepository;

import java.time.LocalDate;
import java.util.List;

import static tn.eps.scolarite.inscription.entities.StatutInscription.*;

/**
 * 15 inscriptions cohérentes avec les jeux de données des autres services :
 * etudiantId 1..10 (etudiant-service) et coursId 1..10 (cours-service).
 * Le cours 6 (BIGD-TP, capacité 3) est rempli => démonstration du 409 "cours complet".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JeuDeDonneesInscriptions implements CommandLineRunner {

    private static final String ANNEE = "2026-2027";

    private final InscriptionRepository inscriptionRepository;

    @Override
    public void run(String... args) {
        if (inscriptionRepository.count() > 0) {
            log.info("Table inscriptions déjà alimentée ({} lignes) : pas de seeding", inscriptionRepository.count());
            return;
        }
        List<Inscription> inscriptions = List.of(
                ins(1L, 1L, "2026-09-15", VALIDEE, 16.5),
                ins(1L, 3L, "2026-09-15", VALIDEE, 14.0),
                ins(1L, 5L, "2026-09-16", VALIDEE, 12.0),
                ins(2L, 1L, "2026-09-15", VALIDEE, 17.75),
                ins(2L, 2L, "2026-09-17", VALIDEE, 18.0),
                ins(2L, 6L, "2026-09-18", VALIDEE, 15.0),
                ins(3L, 1L, "2026-09-16", VALIDEE, 13.0),
                ins(3L, 3L, "2026-09-16", VALIDEE, 8.5),
                ins(3L, 6L, "2026-09-19", EN_ATTENTE, null),
                ins(4L, 2L, "2026-09-20", VALIDEE, 11.25),
                ins(4L, 6L, "2026-09-20", VALIDEE, null),
                ins(5L, 7L, "2026-09-21", VALIDEE, 10.5),
                ins(5L, 8L, "2026-09-21", ANNULEE, null),
                ins(6L, 5L, "2026-09-22", VALIDEE, 15.25),
                ins(7L, 9L, "2026-10-01", EN_ATTENTE, null)
        );
        inscriptionRepository.saveAll(inscriptions);
        log.info("Seeding inscription_db : {} inscriptions insérées", inscriptions.size());
    }

    private static Inscription ins(Long etudiantId, Long coursId, String date, StatutInscription statut, Double note) {
        return Inscription.builder()
                .etudiantId(etudiantId).coursId(coursId)
                .dateInscription(LocalDate.parse(date))
                .anneeUniversitaire(ANNEE)
                .statut(statut).note(note)
                .build();
    }
}
