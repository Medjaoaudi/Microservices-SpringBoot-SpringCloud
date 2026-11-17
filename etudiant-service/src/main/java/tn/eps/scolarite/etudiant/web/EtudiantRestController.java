package tn.eps.scolarite.etudiant.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.eps.scolarite.etudiant.dto.EtudiantRequestDTO;
import tn.eps.scolarite.etudiant.dto.EtudiantResponseDTO;
import tn.eps.scolarite.etudiant.entities.Filiere;
import tn.eps.scolarite.etudiant.exceptions.ApiError;
import tn.eps.scolarite.etudiant.services.EtudiantService;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
@Tag(name = "Étudiants", description = "Gestion du référentiel des étudiants")
public class EtudiantRestController {

    private final EtudiantService etudiantService;

    @GetMapping
    @Operation(summary = "Lister les étudiants (paginé)",
            description = "Paramètres : page (0..n), size, sort (ex. nom,asc)")
    public Page<EtudiantResponseDTO> lister(
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return etudiantService.lister(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un étudiant par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant trouvé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EtudiantResponseDTO trouverParId(@Parameter(example = "1") @PathVariable Long id) {
        return etudiantService.trouverParId(id);
    }

    @GetMapping("/matricule/{matricule}")
    @Operation(summary = "Consulter un étudiant par son matricule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant trouvé"),
            @ApiResponse(responseCode = "404", description = "Matricule inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EtudiantResponseDTO trouverParMatricule(@Parameter(example = "ETU-2026-001") @PathVariable String matricule) {
        return etudiantService.trouverParMatricule(matricule);
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche multicritère (nom/prénom, filière, niveau)")
    public List<EtudiantResponseDTO> rechercher(
            @Parameter(example = "ben") @RequestParam(required = false) String nom,
            @Parameter(example = "GL") @RequestParam(required = false) Filiere filiere,
            @Parameter(example = "5") @RequestParam(required = false) Integer niveau) {
        return etudiantService.rechercher(nom, filiere, niveau);
    }

    @GetMapping("/exists/{id}")
    @Operation(summary = "Vérifier l'existence d'un étudiant (utilisé par l'orchestrateur)")
    public boolean existe(@Parameter(example = "1") @PathVariable Long id) {
        return etudiantService.existe(id);
    }

    @PostMapping
    @Operation(summary = "Créer un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Étudiant créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Matricule ou email déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EtudiantResponseDTO> creer(@Valid @RequestBody EtudiantRequestDTO dto) {
        EtudiantResponseDTO cree = etudiantService.creer(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Étudiant modifié"),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Matricule ou email déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EtudiantResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody EtudiantRequestDTO dto) {
        return etudiantService.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un étudiant")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Étudiant supprimé"),
            @ApiResponse(responseCode = "404", description = "Étudiant introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void supprimer(@PathVariable Long id) {
        etudiantService.supprimer(id);
    }
}
