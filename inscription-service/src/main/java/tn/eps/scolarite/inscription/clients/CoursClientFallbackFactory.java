package tn.eps.scolarite.inscription.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.eps.scolarite.inscription.exceptions.RessourceDistanteIntrouvableException;
import tn.eps.scolarite.inscription.exceptions.ServicePartenaireIndisponibleException;
import tn.eps.scolarite.inscription.model.Capacite;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.SourceDonnees;

/** Fallback de COURS-SERVICE (même stratégie que pour les étudiants). */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoursClientFallbackFactory implements FallbackFactory<CoursRestClient> {

    private final PartenairesCache cache;

    @Override
    public CoursRestClient create(Throwable cause) {
        return new CoursRestClient() {
            @Override
            public Cours findById(Long id) {
                if (FallbackSupport.estIntrouvable(cause)) {
                    throw new RessourceDistanteIntrouvableException("Cours introuvable (id=" + id + ")");
                }
                log.warn("[FALLBACK] COURS-SERVICE indisponible pour coursId={} : {}", id,
                        FallbackSupport.decrire(cause));
                return cache.cours(id)
                        .map(c -> c.toBuilder().sourceDonnees(SourceDonnees.CACHE_SECOURS).build())
                        .orElseGet(() -> Cours.indisponible(id));
            }

            @Override
            public Capacite getCapacite(Long id) {
                if (FallbackSupport.estIntrouvable(cause)) {
                    throw new RessourceDistanteIntrouvableException("Cours introuvable (id=" + id + ")");
                }
                log.warn("[FALLBACK] COURS-SERVICE indisponible (capacité du cours {}) : {}", id,
                        FallbackSupport.decrire(cause));
                // Aucune valeur "par défaut" n'a de sens pour une capacité : on refuse explicitement.
                throw new ServicePartenaireIndisponibleException("COURS-SERVICE",
                        "Impossible de vérifier la capacité du cours " + id + " : COURS-SERVICE est indisponible");
            }
        };
    }
}
