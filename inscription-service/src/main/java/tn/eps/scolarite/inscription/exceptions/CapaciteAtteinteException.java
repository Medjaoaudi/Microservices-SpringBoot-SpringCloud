package tn.eps.scolarite.inscription.exceptions;

/** Le cours est complet -> 409. */
public class CapaciteAtteinteException extends RuntimeException {
    public CapaciteAtteinteException(String message) {
        super(message);
    }
}
