package tn.eps.scolarite.etudiant.exceptions;

/** Violation d'unicité métier (matricule, email...) -> HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
