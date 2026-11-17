package tn.eps.scolarite.inscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.inscription.entities.StatutInscription;
import tn.eps.scolarite.inscription.model.Cours;
import tn.eps.scolarite.inscription.model.Etudiant;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "Inscription enrichie : données locales + étudiant et cours récupérés via OpenFeign")
public record InscriptionResponseDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long etudiantId,
        @Schema(example = "1") Long coursId,
        LocalDate dateInscription,
        @Schema(example = "2026-2027") String anneeUniversitaire,
        @Schema(example = "VALIDEE") StatutInscription statut,
        @Schema(example = "16.5") Double note,
        @Schema(description = "Champ @Transient rempli via ETUDIANT-SERVICE") Etudiant etudiant,
        @Schema(description = "Champ @Transient rempli via COURS-SERVICE") Cours cours,
        @Schema(description = "État des services partenaires lors de l'agrégation (UP / CACHE_SECOURS / INDISPONIBLE)",
                example = "{\"ETUDIANT-SERVICE\":\"UP\",\"COURS-SERVICE\":\"UP\"}")
        Map<String, String> serviceStatus,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Avertissements de dégradation éventuels")
        List<String> warnings
) {
}
