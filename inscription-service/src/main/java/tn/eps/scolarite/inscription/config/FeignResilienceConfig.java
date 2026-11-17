package tn.eps.scolarite.inscription.config;

import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des clients OpenFeign et de leur tolérance aux pannes.
 * <ul>
 *   <li>Retry : jusqu'à N tentatives avec backoff exponentiel (période x1.5, plafonnée) sur les erreurs
 *       réseau "retryables" (connexion refusée, read timeout). Tous nos appels Feign sont des lectures (GET).</li>
 *   <li>Un disjoncteur PAR SERVICE PARTENAIRE ("ETUDIANT-SERVICE", "COURS-SERVICE") au lieu d'un par méthode,
 *       pour une lecture claire dans /actuator/circuitbreakers.</li>
 * </ul>
 * Les seuils du Circuit Breaker et du TimeLimiter sont dans application.yml (resilience4j.*).
 */
@Configuration
@EnableFeignClients(basePackages = "tn.eps.scolarite.inscription.clients")
public class FeignResilienceConfig {

    @Bean
    public Retryer feignRetryer(@Value("${scolarite.feign.retry.period-ms:200}") long periodeMs,
                                @Value("${scolarite.feign.retry.max-period-ms:1000}") long periodeMaxMs,
                                @Value("${scolarite.feign.retry.max-attempts:3}") int tentatives) {
        return new Retryer.Default(periodeMs, periodeMaxMs, tentatives);
    }

    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (feignClientName, target, method) -> feignClientName;
    }
}
