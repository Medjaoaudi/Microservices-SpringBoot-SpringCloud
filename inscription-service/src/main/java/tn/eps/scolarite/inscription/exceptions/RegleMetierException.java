package tn.eps.scolarite.inscription.exceptions;

/** Transition ou opération interdite par les règles de gestion (ex. noter une inscription annulée) -> 409. */
public class RegleMetierException extends RuntimeException {
    public RegleMetierException(String message) {
        super(message);
    }
}
