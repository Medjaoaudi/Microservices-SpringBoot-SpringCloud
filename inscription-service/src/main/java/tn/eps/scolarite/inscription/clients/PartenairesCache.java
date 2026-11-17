package tn.eps.scolarite.inscription.clients;

import org.springframework.stereotype.Component;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.Etudiant;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache local de secours : mémorise la dernière version connue des étudiants et des cours
 * récupérés AVEC SUCCÈS. Les fallbacks l'utilisent avant de renvoyer l'objet "INDISPONIBLE".
 * (ConcurrentHashMap : thread-safe, suffisant pour la démo ; Caffeine/Redis en production.)
 */
@Component
public class PartenairesCache {

    private final Map<Long, Etudiant> etudiants = new ConcurrentHashMap<>();
    private final Map<Long, Cours> cours = new ConcurrentHashMap<>();

    public void memoriser(Etudiant e) {
        if (e != null && e.getId() != null && !e.isDegrade()) {
            etudiants.put(e.getId(), e);
        }
    }

    public void memoriser(Cours c) {
        if (c != null && c.getId() != null && !c.isDegrade()) {
            cours.put(c.getId(), c);
        }
    }

    public Optional<Etudiant> etudiant(Long id) {
        return Optional.ofNullable(etudiants.get(id));
    }

    public Optional<Cours> cours(Long id) {
        return Optional.ofNullable(cours.get(id));
    }

    public void oublierEtudiant(Long id) {
        etudiants.remove(id);
    }

    public void oublierCours(Long id) {
        cours.remove(id);
    }

    public int tailleEtudiants() {
        return etudiants.size();
    }

    public int tailleCours() {
        return cours.size();
    }
}
