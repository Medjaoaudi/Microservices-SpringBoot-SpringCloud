package tn.eps.scolarite.cours.exceptions;

/** Ressource demandée inexistante -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
