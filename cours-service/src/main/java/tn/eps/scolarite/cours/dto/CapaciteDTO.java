package tn.eps.scolarite.cours.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Capacité d'accueil d'un cours")
public record CapaciteDTO(
        @Schema(example = "6") Long coursId,
        @Schema(example = "BIGD-TP") String code,
        @Schema(example = "3") Integer capaciteMax
) {
}
