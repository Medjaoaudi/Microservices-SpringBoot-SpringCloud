package tn.eps.scolarite.cours.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import tn.eps.scolarite.cours.entities.TypeCours;

@Schema(description = "Données de création / modification d'un cours")
public record CoursRequestDTO(
        @Schema(example = "JEE2-TD")
        @NotBlank(message = "Le code est obligatoire")
        @Size(max = 20)
        String code,

        @Schema(example = "TD Spring Cloud Gateway")
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150)
        String titre,

        @Schema(example = "Routage, filtres et prédicats avec Spring Cloud Gateway")
        @Size(max = 1000)
        String description,

        @Schema(example = "21")
        @NotNull(message = "Le volume horaire est obligatoire")
        @Min(value = 1, message = "Volume horaire minimum : 1 h")
        @Max(value = 200, message = "Volume horaire maximum : 200 h")
        Integer volumeHoraire,

        @Schema(example = "Dr. Hela Ben Amor")
        @NotBlank(message = "L'enseignant est obligatoire")
        String enseignant,

        @Schema(example = "30")
        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "Capacité minimum : 1")
        @Max(value = 500, message = "Capacité maximum : 500")
        Integer capaciteMax,

        @Schema(example = "TD")
        @NotNull(message = "Le type est obligatoire")
        TypeCours type,

        @Schema(example = "1")
        @NotNull(message = "Le module de rattachement est obligatoire")
        Long moduleId
) {
}
