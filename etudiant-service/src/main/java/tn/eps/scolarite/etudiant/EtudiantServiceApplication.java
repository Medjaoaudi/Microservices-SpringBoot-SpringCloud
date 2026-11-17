package tn.eps.scolarite.etudiant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservice "Étudiants" : référentiel des étudiants inscrits à l'université.
 * Base de données propre : etudiant_db.
 */
@SpringBootApplication
public class EtudiantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EtudiantServiceApplication.class, args);
    }
}
