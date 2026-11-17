package tn.eps.scolarite.inscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(description = "Demande d'inscription d'un étudiant à un cours")
public record InscriptionRequestDTO(
        @Schema(example = "8", description = "Identifiant de l'étudiant (ETUDIANT-SERVICE)")
        @NotNull(message = "etudiantId est obligatoire")
        @Positive
        Long etudiantId,

        @Schema(example = "1", description = "Identifiant du cours (COURS-SERVICE)")
        @NotNull(message = "coursId est obligatoire")
        @Positive
        Long coursId,

        @Schema(example = "2026-2027", description = "Facultatif : année universitaire courante par défaut")
        @Pattern(regexp = "\\d{4}-\\d{4}", message = "Format attendu : AAAA-AAAA (ex. 2026-2027)")
        String anneeUniversitaire
) {
}
