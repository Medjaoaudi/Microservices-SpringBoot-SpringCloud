package tn.eps.scolarite.inscription.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.eps.scolarite.inscription.model.Capacite;
import tn.eps.scolarite.inscription.model.Cours;

/** Client déclaratif vers COURS-SERVICE (résolution Eureka, disjoncteur "COURS-SERVICE"). */
@FeignClient(name = "COURS-SERVICE", fallbackFactory = CoursClientFallbackFactory.class)
public interface CoursRestClient {

    @GetMapping("/api/cours/{id}")
    Cours findById(@PathVariable("id") Long id);

    @GetMapping("/api/cours/{id}/capacite")
    Capacite getCapacite(@PathVariable("id") Long id);
}
