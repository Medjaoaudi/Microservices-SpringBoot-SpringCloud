package tn.eps.scolarite.inscription.model;

/**
 * Origine d'un objet distant renvoyé par un fallback.
 * Absent (null) dans le JSON quand la donnée vient directement du service partenaire.
 */
public enum SourceDonnees {
    /** Dernière valeur connue, conservée dans le cache local de secours. */
    CACHE_SECOURS,
    /** Aucune donnée disponible : objet "bouchon" minimal. */
    INDISPONIBLE
}
