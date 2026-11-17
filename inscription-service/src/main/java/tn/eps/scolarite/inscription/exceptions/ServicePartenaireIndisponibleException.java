package tn.eps.scolarite.inscription.exceptions;

import lombok.Getter;

/** Un service partenaire est hors ligne et l'opération ne peut pas être validée -> 503. */
@Getter
public class ServicePartenaireIndisponibleException extends RuntimeException {

    private final String service;

    public ServicePartenaireIndisponibleException(String service, String message) {
        super(message);
        this.service = service;
    }
}
