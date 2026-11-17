package tn.eps.scolarite.etudiant.exceptions;

/** Ressource demandée inexistante -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
