package tn.eps.scolarite.inscription.exceptions;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduction centralisée des exceptions en réponses JSON uniformes
 * {timestamp, status, error, message, path} : 400 / 404 / 409 / 503. Jamais de stacktrace côté client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> details.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", "Données invalides", req.getRequestURI(), details));
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> requeteIncorrecte(Exception ex, HttpServletRequest req) {
        String message = ex instanceof HttpMessageNotReadableException
                ? "Corps de requête JSON illisible ou valeur invalide (ex. statut inconnu)"
                : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler({ResourceNotFoundException.class, RessourceDistanteIntrouvableException.class})
    public ResponseEntity<ApiError> introuvable(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> routeInconnue(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Ressource inexistante : " + req.getRequestURI(), req);
    }

    @ExceptionHandler({InscriptionEnDoubleException.class, CapaciteAtteinteException.class, RegleMetierException.class})
    public ResponseEntity<ApiError> conflit(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(ServicePartenaireIndisponibleException.class)
    public ResponseEntity<ApiError> partenaireIndisponible(ServicePartenaireIndisponibleException ex,
                                                           HttpServletRequest req) {
        log.warn("Requête {} refusée (503) : {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "10")
                .body(ApiError.of(503, "Service Unavailable", ex.getMessage(), req.getRequestURI(),
                        Map.of("service", ex.getService())));
    }

    /** Filet de sécurité : erreur Feign/disjoncteur qui n'aurait pas été absorbée par un fallback. */
    @ExceptionHandler({FeignException.class, NoFallbackAvailableException.class})
    public ResponseEntity<ApiError> erreurPartenaire(RuntimeException ex, HttpServletRequest req) {
        log.warn("Erreur d'appel à un service partenaire sur {} : {}", req.getRequestURI(), ex.toString());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Un service partenaire n'a pas pu répondre correctement", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inattendue(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue sur {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }
}
