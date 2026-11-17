package tn.eps.scolarite.inscription.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

/**
 * Représentation LOCALE d'un étudiant appartenant à ETUDIANT-SERVICE.
 * Simple POJO (pas une @Entity) : désérialisé depuis la réponse JSON de Feign.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Etudiant {
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private String filiere;
    private Integer niveau;

    /** Renseigné uniquement par le fallback (CACHE_SECOURS ou INDISPONIBLE). */
    private SourceDonnees sourceDonnees;

    /** Objet "bouchon" renvoyé quand le service est hors ligne et que le cache est vide. */
    public static Etudiant indisponible(Long id) {
        return Etudiant.builder()
                .id(id)
                .nom("INDISPONIBLE")
                .prenom("Service étudiant hors ligne")
                .sourceDonnees(SourceDonnees.INDISPONIBLE)
                .build();
    }

    @JsonIgnore
    public boolean isDegrade() {
        return sourceDonnees != null;
    }
}
