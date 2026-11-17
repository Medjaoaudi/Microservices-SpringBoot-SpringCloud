package tn.eps.scolarite.inscription.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.eps.scolarite.inscription.exceptions.RessourceDistanteIntrouvableException;
import tn.eps.scolarite.inscription.model.Etudiant;
import tn.eps.scolarite.inscription.model.SourceDonnees;

/**
 * Fallback de ETUDIANT-SERVICE. Reçoit la cause de l'échec (exception réseau, timeout,
 * circuit ouvert...) et renvoie une implémentation de secours du client :
 * <ol>
 *     <li>404 du partenaire : ce n'est pas une panne -> on propage "introuvable" ;</li>
 *     <li>sinon : dernière valeur connue (cache de secours) marquée CACHE_SECOURS ;</li>
 *     <li>à défaut : objet bouchon "INDISPONIBLE".</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtudiantClientFallbackFactory implements FallbackFactory<EtudiantRestClient> {

    private final PartenairesCache cache;

    @Override
    public EtudiantRestClient create(Throwable cause) {
        return new EtudiantRestClient() {
            @Override
            public Etudiant findById(Long id) {
                if (FallbackSupport.estIntrouvable(cause)) {
                    throw new RessourceDistanteIntrouvableException("Étudiant introuvable (id=" + id + ")");
                }
                log.warn("[FALLBACK] ETUDIANT-SERVICE indisponible pour etudiantId={} : {}", id,
                        FallbackSupport.decrire(cause));
                return cache.etudiant(id)
                        .map(e -> e.toBuilder().sourceDonnees(SourceDonnees.CACHE_SECOURS).build())
                        .orElseGet(() -> Etudiant.indisponible(id));
            }
        };
    }
}
