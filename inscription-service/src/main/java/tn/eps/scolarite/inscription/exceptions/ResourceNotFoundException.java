package tn.eps.scolarite.inscription.exceptions;

/** Inscription (ressource locale) inexistante -> 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
