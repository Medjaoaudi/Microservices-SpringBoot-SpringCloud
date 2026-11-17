package tn.eps.scolarite.inscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.inscription.entities.StatutInscription;

@Schema(description = "Ligne du relevé de notes (un cours)")
public record LigneReleveDTO(
        @Schema(example = "1") Long inscriptionId,
        @Schema(example = "2026-2027") String anneeUniversitaire,
        @Schema(example = "1") Long coursId,
        @Schema(example = "JEE2-C") String coursCode,
        @Schema(example = "Microservices avec Spring Cloud") String coursTitre,
        @Schema(example = "COURS") String typeCours,
        @Schema(example = "UE-JEE2") String moduleCode,
        @Schema(example = "Développement JEE avancé (JEE2)") String moduleIntitule,
        @Schema(example = "3.0") Double coefficient,
        @Schema(example = "5") Integer credits,
        @Schema(example = "16.5") Double note,
        @Schema(example = "VALIDEE") StatutInscription statut
) {
}
