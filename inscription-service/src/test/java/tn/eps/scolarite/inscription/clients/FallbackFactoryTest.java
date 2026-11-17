package tn.eps.scolarite.inscription.clients;

import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import tn.eps.scolarite.inscription.exceptions.RessourceDistanteIntrouvableException;
import tn.eps.scolarite.inscription.exceptions.ServicePartenaireIndisponibleException;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.Etudiant;
import tn.eps.scolarite.inscription.model.SourceDonnees;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Comportement des FallbackFactory selon la cause de l'échec Feign. */
class FallbackFactoryTest {

    private final PartenairesCache cache = new PartenairesCache();
    private final EtudiantClientFallbackFactory etudiantFallback = new EtudiantClientFallbackFactory(cache);
    private final CoursClientFallbackFactory coursFallback = new CoursClientFallbackFactory(cache);

    private static final Throwable PANNE_RESEAU = new RuntimeException(new ConnectException("Connection refused"));

    private static FeignException.NotFound notFound() {
        Request req = Request.create(Request.HttpMethod.GET, "http://ETUDIANT-SERVICE/api/etudiants/99",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("404 Not Found", req, null, null);
    }

    @Test
    void panne_sansCache_objetIndisponible() {
        Etudiant e = etudiantFallback.create(PANNE_RESEAU).findById(7L);

        assertThat(e.getId()).isEqualTo(7L);
        assertThat(e.getNom()).isEqualTo("INDISPONIBLE");
        assertThat(e.getSourceDonnees()).isEqualTo(SourceDonnees.INDISPONIBLE);
    }

    @Test
    void panne_avecCache_derniereValeurConnue() {
        cache.memoriser(Etudiant.builder().id(1L).nom("Ben Salah").prenom("Amine").build());

        Etudiant e = etudiantFallback.create(PANNE_RESEAU).findById(1L);

        assertThat(e.getNom()).isEqualTo("Ben Salah");
        assertThat(e.getSourceDonnees()).isEqualTo(SourceDonnees.CACHE_SECOURS);
        // le cache lui-même n'est pas altéré
        assertThat(cache.etudiant(1L)).get().extracting(Etudiant::getSourceDonnees).isNull();
    }

    @Test
    void circuitOuvert_fallbackImmediat() {
        CallNotPermittedException ouvert = CallNotPermittedException
                .createCallNotPermittedException(CircuitBreaker.ofDefaults("COURS-SERVICE"));

        Cours c = coursFallback.create(ouvert).findById(3L);

        assertThat(c.isDegrade()).isTrue();
        assertThat(c.getTitre()).isEqualTo("INDISPONIBLE");
    }

    @Test
    void reponse404_nestPasUnePanne() {
        assertThatThrownBy(() -> etudiantFallback.create(notFound()).findById(99L))
                .isInstanceOf(RessourceDistanteIntrouvableException.class);
    }

    @Test
    void capacite_indisponible_refusExplicite() {
        assertThatThrownBy(() -> coursFallback.create(PANNE_RESEAU).getCapacite(1L))
                .isInstanceOf(ServicePartenaireIndisponibleException.class);
    }
}
