package tn.eps.scolarite.inscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.inscription.model.Etudiant;

import java.util.List;

@Schema(description = "Relevé de notes agrégé (bulletin) d'un étudiant")
public record ReleveNotesDTO(
        Etudiant etudiant,
        @Schema(example = "2026-2027", description = "Année filtrée (null = toutes)") String anneeUniversitaire,
        List<LigneReleveDTO> lignes,
        @Schema(example = "14.23", description = "Σ(note × coefficient du module) / Σ(coefficients), cours notés uniquement")
        Double moyennePonderee,
        @Schema(example = "7.5") Double totalCoefficients,
        @Schema(example = "4") int nombreCoursNotes,
        @Schema(example = "9", description = "Crédits ECTS des modules dont la moyenne est >= 10")
        int creditsAcquis,
        @Schema(example = "Bien") String mention,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> warnings
) {
}
