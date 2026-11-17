package tn.eps.scolarite.inscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import tn.eps.scolarite.inscription.entities.StatutInscription;

@Schema(description = "Nouveau statut d'une inscription")
public record StatutUpdateDTO(
        @Schema(example = "VALIDEE")
        @NotNull(message = "Le statut est obligatoire")
        StatutInscription statut
) {
}
