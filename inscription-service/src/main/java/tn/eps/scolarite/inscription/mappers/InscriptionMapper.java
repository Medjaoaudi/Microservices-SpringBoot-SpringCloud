package tn.eps.scolarite.inscription.mappers;

import org.springframework.stereotype.Component;
import tn.eps.scolarite.inscription.dto.InscriptionResponseDTO;
import tn.eps.scolarite.inscription.dto.LigneReleveDTO;
import tn.eps.scolarite.inscription.entities.Inscription;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.ModuleInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InscriptionMapper {

    public InscriptionResponseDTO toDto(Inscription i) {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("ETUDIANT-SERVICE", etat(i.getEtudiant() == null ? null
                : i.getEtudiant().getSourceDonnees() == null ? "UP" : i.getEtudiant().getSourceDonnees().name()));
        status.put("COURS-SERVICE", etat(i.getCours() == null ? null
                : i.getCours().getSourceDonnees() == null ? "UP" : i.getCours().getSourceDonnees().name()));
        return new InscriptionResponseDTO(i.getId(), i.getEtudiantId(), i.getCoursId(), i.getDateInscription(),
                i.getAnneeUniversitaire(), i.getStatut(), i.getNote(), i.getEtudiant(), i.getCours(),
                status, List.copyOf(i.getWarnings()));
    }

    public LigneReleveDTO toLigne(Inscription i) {
        Cours c = i.getCours();
        ModuleInfo m = c == null ? null : c.getModule();
        return new LigneReleveDTO(i.getId(), i.getAnneeUniversitaire(), i.getCoursId(),
                c == null ? null : c.getCode(),
                c == null ? null : c.getTitre(),
                c == null ? null : c.getType(),
                m == null ? null : m.getCode(),
                m == null ? null : m.getIntitule(),
                m == null ? null : m.getCoefficient(),
                m == null ? null : m.getCredits(),
                i.getNote(), i.getStatut());
    }

    /** "UP" si données fraîches, sinon la source de secours ; "INTROUVABLE" si la ressource n'existe plus. */
    private static String etat(String valeur) {
        return valeur == null ? "INTROUVABLE" : valeur;
    }
}
