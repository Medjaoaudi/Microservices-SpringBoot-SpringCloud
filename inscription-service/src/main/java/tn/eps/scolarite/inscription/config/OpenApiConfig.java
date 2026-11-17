package tn.eps.scolarite.inscription.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Inscriptions (orchestrateur) - Scolarité universitaire",
        version = "1.0.0",
        description = "Microservice inscription-service : inscriptions pédagogiques, notes et relevés. "
                + "Agrège ETUDIANT-SERVICE et COURS-SERVICE via OpenFeign (champs @Transient), "
                + "protégé par Resilience4j (Circuit Breaker, TimeLimiter, Retry, Fallback + cache de secours). "
                + "Base de données dédiée : inscription_db.",
        contact = @Contact(name = "Projet JEE2 - 5ème GL")))
public class OpenApiConfig {
}
