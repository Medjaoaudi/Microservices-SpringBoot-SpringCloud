package tn.eps.scolarite.etudiant.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.eps.scolarite.etudiant.entities.Etudiant;
import tn.eps.scolarite.etudiant.entities.Filiere;
import tn.eps.scolarite.etudiant.repositories.EtudiantRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Jeu de données fictif inséré au premier démarrage (table vide uniquement).
 * Les identifiants 1..10 sont référencés par inscription-service (seeding cohérent).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JeuDeDonneesEtudiants implements CommandLineRunner {

    private final EtudiantRepository etudiantRepository;

    @Override
    public void run(String... args) {
        if (etudiantRepository.count() > 0) {
            log.info("Table etudiants déjà alimentée ({} lignes) : pas de seeding", etudiantRepository.count());
            return;
        }
        List<Etudiant> etudiants = List.of(
                etu("ETU-2026-001", "Ben Salah", "Amine", "+216 22 145 301", "2002-03-14", Filiere.GL, 5),
                etu("ETU-2026-002", "Trabelsi", "Yasmine", "+216 98 220 417", "2002-07-02", Filiere.GL, 5),
                etu("ETU-2026-003", "Gharbi", "Mohamed Aziz", "+216 55 310 982", "2001-11-23", Filiere.GL, 5),
                etu("ETU-2026-004", "Jaziri", "Sarra", "+216 29 874 120", "2002-01-30", Filiere.GL, 5),
                etu("ETU-2026-005", "Mejri", "Oussama", "+216 50 663 245", "2003-05-18", Filiere.RT, 4),
                etu("ETU-2026-006", "Bouazizi", "Nour", "+216 97 412 006", "2002-09-09", Filiere.IIA, 5),
                etu("ETU-2026-007", "Hammami", "Khalil", "+216 24 158 773", "2002-12-01", Filiere.GL, 5),
                etu("ETU-2026-008", "Chaabane", "Rania", "+216 53 902 418", "2004-02-25", Filiere.IMI, 3),
                etu("ETU-2026-009", "Ferchichi", "Skander", "+216 21 337 590", "2002-06-11", Filiere.GL, 5),
                etu("ETU-2026-010", "Dridi", "Emna", "+216 92 640 851", "2002-10-07", Filiere.RT, 5)
        );
        etudiantRepository.saveAll(etudiants);
        log.info("Seeding etudiant_db : {} étudiants insérés", etudiants.size());
    }

    private static Etudiant etu(String matricule, String nom, String prenom, String tel,
                                String naissance, Filiere filiere, int niveau) {
        String email = (prenom.split(" ")[0] + "." + nom.replace(" ", "")).toLowerCase() + "@etudiant.eps.tn";
        return Etudiant.builder()
                .matricule(matricule).nom(nom).prenom(prenom).email(email).telephone(tel)
                .dateNaissance(LocalDate.parse(naissance)).filiere(filiere).niveau(niveau)
                .build();
    }
}
