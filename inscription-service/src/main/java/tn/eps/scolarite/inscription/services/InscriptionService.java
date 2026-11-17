package tn.eps.scolarite.inscription.services;

import tn.eps.scolarite.inscription.dto.InscriptionRequestDTO;
import tn.eps.scolarite.inscription.dto.InscriptionResponseDTO;
import tn.eps.scolarite.inscription.dto.ReleveNotesDTO;
import tn.eps.scolarite.inscription.entities.StatutInscription;

import java.util.List;

public interface InscriptionService {

    List<InscriptionResponseDTO> lister();

    InscriptionResponseDTO trouverParId(Long id);

    List<InscriptionResponseDTO> parEtudiant(Long etudiantId);

    List<InscriptionResponseDTO> parCours(Long coursId);

    InscriptionResponseDTO inscrire(InscriptionRequestDTO dto);

    InscriptionResponseDTO changerStatut(Long id, StatutInscription statut);

    InscriptionResponseDTO attribuerNote(Long id, Double note);

    void supprimer(Long id);

    ReleveNotesDTO releve(Long etudiantId, String anneeUniversitaire);
}
