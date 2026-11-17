package tn.eps.scolarite.cours.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.cours.entities.TypeCours;

@Schema(description = "Cours exposé par l'API (avec résumé de son module)")
public record CoursResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "JEE2-C") String code,
        @Schema(example = "Microservices avec Spring Cloud") String titre,
        String description,
        @Schema(example = "21") Integer volumeHoraire,
        @Schema(example = "Dr. Hela Ben Amor") String enseignant,
        @Schema(example = "40") Integer capaciteMax,
        @Schema(example = "COURS") TypeCours type,
        ModuleResumeDTO module
) {
}
