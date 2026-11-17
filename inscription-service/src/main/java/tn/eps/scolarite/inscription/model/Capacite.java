package tn.eps.scolarite.inscription.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Réponse de GET /api/cours/{id}/capacite. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Capacite {
    private Long coursId;
    private String code;
    private Integer capaciteMax;
}
