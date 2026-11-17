package tn.eps.scolarite.inscription.exceptions;

/** Le service partenaire a répondu, mais la ressource (étudiant/cours) n'existe pas -> 404. */
public class RessourceDistanteIntrouvableException extends RuntimeException {
    public RessourceDistanteIntrouvableException(String message) {
        super(message);
    }
}
