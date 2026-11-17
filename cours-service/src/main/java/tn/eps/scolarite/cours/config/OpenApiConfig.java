package tn.eps.scolarite.cours.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Catalogue (Modules & Cours) - Scolarité universitaire",
        version = "1.0.0",
        description = "Microservice cours-service : unités d'enseignement (modules) et cours/TD/TP, "
                + "relation @OneToMany Module -> Cours. Base de données dédiée : cours_db.",
        contact = @Contact(name = "Projet JEE2 - 5ème GL")))
public class OpenApiConfig {
}
