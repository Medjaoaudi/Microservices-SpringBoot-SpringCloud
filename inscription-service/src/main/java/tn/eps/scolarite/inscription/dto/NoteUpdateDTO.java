package tn.eps.scolarite.inscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Note obtenue (sur 20)")
public record NoteUpdateDTO(
        @Schema(example = "15.5", minimum = "0", maximum = "20")
        @NotNull(message = "La note est obligatoire")
        @DecimalMin(value = "0.0", message = "La note minimale est 0")
        @DecimalMax(value = "20.0", message = "La note maximale est 20")
        Double note
) {
}
