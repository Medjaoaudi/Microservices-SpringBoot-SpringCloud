package tn.eps.scolarite.cours.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tn.eps.scolarite.cours.entities.Semestre;

/** Vue réduite du module embarquée dans un cours (évite la boucle Module -> Cours -> Module). */
@Schema(description = "Résumé du module parent d'un cours")
public record ModuleResumeDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "UE-JEE2") String code,
        @Schema(example = "Développement JEE avancé (JEE2)") String intitule,
        @Schema(example = "S9") Semestre semestre,
        @Schema(example = "3.0") Double coefficient,
        @Schema(example = "5") Integer credits
) {
}
