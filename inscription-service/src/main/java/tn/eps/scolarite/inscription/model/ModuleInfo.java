package tn.eps.scolarite.inscription.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Résumé du module (UE) parent d'un cours, tel que renvoyé par COURS-SERVICE. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleInfo {
    private Long id;
    private String code;
    private String intitule;
    private String semestre;
    private Double coefficient;
    private Integer credits;
}
