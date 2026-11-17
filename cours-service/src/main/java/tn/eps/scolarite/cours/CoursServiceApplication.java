package tn.eps.scolarite.cours;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservice "Catalogue" : unités d'enseignement (modules) et leurs cours/TD/TP.
 * Base de données propre : cours_db.
 */
@SpringBootApplication
public class CoursServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoursServiceApplication.class, args);
    }
}
