package tn.eps.scolarite.inscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservice ORCHESTRATEUR des inscriptions pédagogiques.
 * Il ne stocke que des identifiants (etudiantId, coursId) dans inscription_db et
 * agrège à la volée les données des services ETUDIANT-SERVICE et COURS-SERVICE via OpenFeign.
 */
@SpringBootApplication
public class InscriptionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InscriptionServiceApplication.class, args);
    }
}
