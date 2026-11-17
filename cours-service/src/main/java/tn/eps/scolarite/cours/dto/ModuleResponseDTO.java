package tn.eps.scolarite.cours.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.cours.entities.Semestre;

@Schema(description = "Module (UE) exposé par l'API")
public record ModuleResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "UE-JEE2") String code,
        @Schema(example = "Développement JEE avancé (JEE2)") String intitule,
        @Schema(example = "S9") Semestre semestre,
        @Schema(example = "3.0") Double coefficient,
        @Schema(example = "5") Integer credits,
        @Schema(example = "2", description = "Nombre de cours rattachés") int nombreCours
) {
}
