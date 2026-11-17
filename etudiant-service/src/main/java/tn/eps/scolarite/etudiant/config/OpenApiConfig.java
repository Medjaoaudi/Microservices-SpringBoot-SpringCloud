package tn.eps.scolarite.etudiant.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Étudiants - Scolarité universitaire",
        version = "1.0.0",
        description = "Microservice etudiant-service : CRUD, recherche et pagination des étudiants. "
                + "Base de données dédiée : etudiant_db.",
        contact = @Contact(name = "Projet JEE2 - 5ème GL")))
public class OpenApiConfig {
}
