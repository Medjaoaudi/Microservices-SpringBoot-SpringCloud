package tn.eps.scolarite.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Annuaire Eureka : chaque microservice s'y enregistre (nom logique + IP + port)
 * et la Gateway / les clients Feign le consultent pour résoudre "lb://NOM-SERVICE".
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
