package tn.eps.scolarite.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Passerelle unique de l'architecture : routage dynamique via Eureka (lb://),
 * CORS global, Swagger agrégé, journalisation et disjoncteur (fallback) par service.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
