package tn.eps.scolarite.etudiant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.etudiant.entities.Filiere;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Étudiant exposé par l'API")
public record EtudiantResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "ETU-2026-001") String matricule,
        @Schema(example = "Ben Salah") String nom,
        @Schema(example = "Amine") String prenom,
        @Schema(example = "amine.bensalah@etudiant.eps.tn") String email,
        @Schema(example = "+216 22 145 301") String telephone,
        LocalDate dateNaissance,
        @Schema(example = "GL") Filiere filiere,
        @Schema(example = "5") Integer niveau,
        LocalDateTime dateCreation
) {
}
