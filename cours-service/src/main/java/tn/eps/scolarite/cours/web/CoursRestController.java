package tn.eps.scolarite.cours.web;

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
import tn.eps.scolarite.cours.dto.CapaciteDTO;
import tn.eps.scolarite.cours.dto.CoursRequestDTO;
import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.exceptions.ApiError;
import tn.eps.scolarite.cours.services.CoursService;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
@Tag(name = "Cours", description = "Cours, TD et TP rattachés à un module")
public class CoursRestController {

    private final CoursService coursService;

    @GetMapping
    @Operation(summary = "Lister tous les cours")
    public List<CoursResponseDTO> lister() {
        return coursService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un cours (avec son module)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cours trouvé"),
            @ApiResponse(responseCode = "404", description = "Cours introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CoursResponseDTO trouver(@Parameter(example = "1") @PathVariable Long id) {
        return coursService.trouverParId(id);
    }

    @GetMapping("/module/{moduleId}")
    @Operation(summary = "Lister les cours d'un module")
    public List<CoursResponseDTO> parModule(@Parameter(example = "1") @PathVariable Long moduleId) {
        return coursService.parModule(moduleId);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher par titre et/ou enseignant")
    public List<CoursResponseDTO> rechercher(
            @Parameter(example = "spring") @RequestParam(required = false) String titre,
            @Parameter(example = "Ben Amor") @RequestParam(required = false) String enseignant) {
        return coursService.rechercher(titre, enseignant);
    }

    @GetMapping("/{id}/capacite")
    @Operation(summary = "Capacité maximale d'un cours (utilisé par l'orchestrateur)")
    public CapaciteDTO capacite(@Parameter(example = "6") @PathVariable Long id) {
        return coursService.capacite(id);
    }

    @PostMapping
    @Operation(summary = "Créer un cours")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cours créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Module inexistant",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Code déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CoursResponseDTO> creer(@Valid @RequestBody CoursRequestDTO dto) {
        CoursResponseDTO cree = coursService.creer(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un cours")
    public CoursResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody CoursRequestDTO dto) {
        return coursService.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un cours")
    public void supprimer(@PathVariable Long id) {
        coursService.supprimer(id);
    }
}
