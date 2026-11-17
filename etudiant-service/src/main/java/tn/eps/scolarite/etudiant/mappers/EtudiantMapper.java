package tn.eps.scolarite.etudiant.mappers;

import org.springframework.stereotype.Component;
import tn.eps.scolarite.etudiant.dto.EtudiantRequestDTO;
import tn.eps.scolarite.etudiant.dto.EtudiantResponseDTO;
import tn.eps.scolarite.etudiant.entities.Etudiant;

/** Conversion Entité <-> DTO (écrit à la main pour rester lisible). */
@Component
public class EtudiantMapper {

    public Etudiant toEntity(EtudiantRequestDTO dto) {
        Etudiant etudiant = new Etudiant();
        updateEntity(etudiant, dto);
        return etudiant;
    }

    public void updateEntity(Etudiant etudiant, EtudiantRequestDTO dto) {
        etudiant.setMatricule(dto.matricule().trim().toUpperCase());
        etudiant.setNom(dto.nom().trim());
        etudiant.setPrenom(dto.prenom().trim());
        etudiant.setEmail(dto.email().trim().toLowerCase());
        etudiant.setTelephone(dto.telephone());
        etudiant.setDateNaissance(dto.dateNaissance());
        etudiant.setFiliere(dto.filiere());
        etudiant.setNiveau(dto.niveau());
    }

    public EtudiantResponseDTO toDto(Etudiant e) {
        return new EtudiantResponseDTO(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(), e.getEmail(),
                e.getTelephone(), e.getDateNaissance(), e.getFiliere(), e.getNiveau(), e.getDateCreation());
    }
}
