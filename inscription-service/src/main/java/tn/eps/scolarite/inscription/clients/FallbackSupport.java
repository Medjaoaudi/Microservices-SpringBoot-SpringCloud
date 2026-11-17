package tn.eps.scolarite.inscription.clients;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.util.concurrent.TimeoutException;

/** Outils communs aux FallbackFactory : analyse de la cause d'échec d'un appel Feign. */
final class FallbackSupport {

    private FallbackSupport() {
    }

    /** Vrai si le partenaire a répondu 404 : ce n'est PAS une panne, la ressource n'existe pas. */
    static boolean estIntrouvable(Throwable cause) {
        Throwable t = cause;
        while (t != null) {
            if (t instanceof FeignException.NotFound) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    /** Libellé court et lisible de la cause (pour les logs). */
    static String decrire(Throwable cause) {
        if (cause == null) {
            return "cause inconnue";
        }
        if (cause instanceof CallNotPermittedException) {
            return "circuit OUVERT (appel court-circuité sans solliciter le service)";
        }
        if (cause instanceof TimeoutException) {
            return "délai dépassé (TimeLimiter)";
        }
        if (cause instanceof FeignException fe) {
            return "HTTP " + fe.status() + " - " + premiereLigne(fe.getMessage());
        }
        return cause.getClass().getSimpleName() + " - " + premiereLigne(cause.getMessage());
    }

    private static String premiereLigne(String message) {
        if (message == null) {
            return "";
        }
        String ligne = message.lines().findFirst().orElse("");
        return ligne.length() > 160 ? ligne.substring(0, 160) + "..." : ligne;
    }
}
