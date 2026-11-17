package tn.eps.scolarite.inscription.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.eps.scolarite.inscription.dto.*;
import tn.eps.scolarite.inscription.exceptions.ApiError;
import tn.eps.scolarite.inscription.services.InscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
@Tag(name = "Inscriptions", description = "Orchestrateur : inscriptions enrichies via OpenFeign (étudiant + cours)")
public class InscriptionRestController {

    private final InscriptionService inscriptionService;

    @GetMapping
    @Operation(summary = "Lister toutes les inscriptions (enrichies)",
            description = "Les champs @Transient 'etudiant' et 'cours' sont remplis via Feign. "
                    + "Si un service partenaire est en panne, la réponse reste 200 avec des données dégradées.")
    public List<InscriptionResponseDTO> lister() {
        return inscriptionService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une inscription (enrichie)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscription trouvée (éventuellement en mode dégradé)"),
            @ApiResponse(responseCode = "404", description = "Inscription introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public InscriptionResponseDTO trouver(@Parameter(example = "1") @PathVariable Long id) {
        return inscriptionService.trouverParId(id);
    }

    @GetMapping("/etudiant/{etudiantId}")
    @Operation(summary = "Inscriptions d'un étudiant (enrichies)")
    public List<InscriptionResponseDTO> parEtudiant(@Parameter(example = "1") @PathVariable Long etudiantId) {
        return inscriptionService.parEtudiant(etudiantId);
    }

    @GetMapping("/etudiant/{etudiantId}/releve")
    @Operation(summary = "Relevé de notes agrégé avec moyenne pondérée par les coefficients des modules")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relevé calculé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ReleveNotesDTO releve(@Parameter(example = "1") @PathVariable Long etudiantId,
                                 @Parameter(example = "2026-2027", description = "Facultatif")
                                 @RequestParam(required = false) String annee) {
        return inscriptionService.releve(etudiantId, annee);
    }

    @GetMapping("/cours/{coursId}")
    @Operation(summary = "Liste des inscrits à un cours (enrichie)")
    public List<InscriptionResponseDTO> parCours(@Parameter(example = "6") @PathVariable Long coursId) {
        return inscriptionService.parCours(coursId);
    }

    @PostMapping
    @Operation(summary = "Inscrire un étudiant à un cours",
            description = "Vérifie via Feign l'existence de l'étudiant et du cours, l'absence de doublon "
                    + "et la capacité restante. Si un service partenaire est indisponible : 503 (aucune inscription à l'aveugle).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscription créée (statut EN_ATTENTE)"),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Étudiant ou cours introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Déjà inscrit ou cours complet",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Service partenaire indisponible",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<InscriptionResponseDTO> inscrire(@Valid @RequestBody InscriptionRequestDTO dto) {
        InscriptionResponseDTO creee = inscriptionService.inscrire(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(creee.id()).toUri();
        return ResponseEntity.created(location).body(creee);
    }

    @PutMapping("/{id}/statut")
    @Operation(summary = "Changer le statut (EN_ATTENTE, VALIDEE, ANNULEE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut modifié"),
            @ApiResponse(responseCode = "404", description = "Inscription introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Transition interdite (réactivation d'une annulation)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public InscriptionResponseDTO changerStatut(@PathVariable Long id, @Valid @RequestBody StatutUpdateDTO dto) {
        return inscriptionService.changerStatut(id, dto.statut());
    }

    @PutMapping("/{id}/note")
    @Operation(summary = "Saisir / corriger la note (0 à 20)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note enregistrée"),
            @ApiResponse(responseCode = "400", description = "Note hors de [0, 20]",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Inscription annulée",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public InscriptionResponseDTO attribuerNote(@PathVariable Long id, @Valid @RequestBody NoteUpdateDTO dto) {
        return inscriptionService.attribuerNote(id, dto.note());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une inscription")
    public void supprimer(@PathVariable Long id) {
        inscriptionService.supprimer(id);
    }
}
