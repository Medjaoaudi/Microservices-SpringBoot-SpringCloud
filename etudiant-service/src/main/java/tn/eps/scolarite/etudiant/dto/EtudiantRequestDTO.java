package tn.eps.scolarite.etudiant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import tn.eps.scolarite.etudiant.entities.Filiere;

import java.time.LocalDate;

@Schema(description = "Données de création / modification d'un étudiant")
public record EtudiantRequestDTO(
        @Schema(example = "ETU-2026-011", description = "Matricule unique (format ETU-AAAA-NNN)")
        @NotBlank(message = "Le matricule est obligatoire")
        @Pattern(regexp = "ETU-\\d{4}-\\d{3}", message = "Le matricule doit respecter le format ETU-AAAA-NNN")
        String matricule,

        @Schema(example = "Karray")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 60)
        String nom,

        @Schema(example = "Malek")
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 60)
        String prenom,

        @Schema(example = "malek.karray@etudiant.eps.tn")
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email,

        @Schema(example = "+216 22 345 678")
        @Pattern(regexp = "^[+0-9 ]{8,20}$", message = "Téléphone invalide")
        String telephone,

        @Schema(example = "2003-04-12", type = "string", format = "date")
        @Past(message = "La date de naissance doit être dans le passé")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateNaissance,

        @Schema(example = "GL")
        @NotNull(message = "La filière est obligatoire")
        Filiere filiere,

        @Schema(example = "5", minimum = "1", maximum = "5")
        @NotNull(message = "Le niveau est obligatoire")
        @Min(value = 1, message = "Le niveau minimum est 1")
        @Max(value = 5, message = "Le niveau maximum est 5")
        Integer niveau
) {
}
