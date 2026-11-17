package tn.eps.scolarite.inscription.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.eps.scolarite.inscription.model.Etudiant;

/**
 * Client déclaratif vers ETUDIANT-SERVICE.
 * Pas d'attribut "url" : l'adresse est résolue dynamiquement par Eureka + Spring Cloud LoadBalancer.
 * Chaque appel passe par le disjoncteur Resilience4j "ETUDIANT-SERVICE" ; en cas d'échec,
 * la FallbackFactory fournit une réponse de secours.
 */
@FeignClient(name = "ETUDIANT-SERVICE", fallbackFactory = EtudiantClientFallbackFactory.class)
public interface EtudiantRestClient {

    @GetMapping("/api/etudiants/{id}")
    Etudiant findById(@PathVariable("id") Long id);
}
