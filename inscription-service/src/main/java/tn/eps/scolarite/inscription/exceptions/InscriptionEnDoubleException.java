package tn.eps.scolarite.inscription.exceptions;

/** L'étudiant est déjà inscrit à ce cours pour cette année -> 409. */
public class InscriptionEnDoubleException extends RuntimeException {
    public InscriptionEnDoubleException(String message) {
        super(message);
    }
}
