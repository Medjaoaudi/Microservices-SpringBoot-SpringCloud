package tn.eps.scolarite.cours.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import tn.eps.scolarite.cours.entities.Semestre;

@Schema(description = "Données de création / modification d'un module (UE)")
public record ModuleRequestDTO(
        @Schema(example = "UE-CLOUD")
        @NotBlank(message = "Le code est obligatoire")
        @Pattern(regexp = "UE-[A-Z0-9]{2,15}", message = "Le code doit respecter le format UE-XXXX")
        String code,

        @Schema(example = "Cloud Computing et DevOps")
        @NotBlank(message = "L'intitulé est obligatoire")
        @Size(max = 150)
        String intitule,

        @Schema(example = "S9")
        @NotNull(message = "Le semestre est obligatoire")
        Semestre semestre,

        @Schema(example = "2.0")
        @NotNull(message = "Le coefficient est obligatoire")
        @DecimalMin(value = "0.5", message = "Coefficient minimum : 0.5")
        @DecimalMax(value = "10", message = "Coefficient maximum : 10")
        Double coefficient,

        @Schema(example = "4")
        @NotNull(message = "Le nombre de crédits est obligatoire")
        @Min(value = 1, message = "Au moins 1 crédit ECTS")
        @Max(value = 30, message = "Au plus 30 crédits ECTS")
        Integer credits
) {
}
