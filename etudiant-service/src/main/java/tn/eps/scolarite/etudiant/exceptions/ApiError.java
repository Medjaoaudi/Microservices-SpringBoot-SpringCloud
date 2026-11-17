package tn.eps.scolarite.etudiant.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/** Format JSON uniforme de toutes les erreurs de l'API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Réponse d'erreur uniforme")
public record ApiError(
        LocalDateTime timestamp,
        @Schema(example = "404") int status,
        @Schema(example = "Not Found") String error,
        @Schema(example = "Étudiant introuvable (id=99)") String message,
        @Schema(example = "/api/etudiants/99") String path,
        Map<String, String> details
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, null);
    }
}
