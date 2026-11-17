package tn.eps.scolarite.inscription.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Représentation LOCALE d'un cours appartenant à COURS-SERVICE (POJO, pas une @Entity).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cours {
    private Long id;
    private String code;
    private String titre;
    private String description;
    private Integer volumeHoraire;
    private String enseignant;
    private Integer capaciteMax;
    private String type;
    private ModuleInfo module;

    /** Renseigné uniquement par le fallback (CACHE_SECOURS ou INDISPONIBLE). */
    private SourceDonnees sourceDonnees;

    public static Cours indisponible(Long id) {
        return Cours.builder()
                .id(id)
                .titre("INDISPONIBLE")
                .description("Service cours hors ligne")
                .sourceDonnees(SourceDonnees.INDISPONIBLE)
                .build();
    }

    @JsonIgnore
    public boolean isDegrade() {
        return sourceDonnees != null;
    }
}
