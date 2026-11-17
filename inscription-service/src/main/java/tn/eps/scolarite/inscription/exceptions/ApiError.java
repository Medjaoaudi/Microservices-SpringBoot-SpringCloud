package tn.eps.scolarite.inscription.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/** Format JSON uniforme de toutes les erreurs de l'API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Réponse d'erreur uniforme")
public record ApiError(
        LocalDateTime timestamp,
        @Schema(example = "503") int status,
        @Schema(example = "Service Unavailable") String error,
        @Schema(example = "ETUDIANT-SERVICE est indisponible : impossible de vérifier l'étudiant 1.") String message,
        @Schema(example = "/api/inscriptions") String path,
        Map<String, String> details
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path, Map<String, String> details) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, details);
    }
}
