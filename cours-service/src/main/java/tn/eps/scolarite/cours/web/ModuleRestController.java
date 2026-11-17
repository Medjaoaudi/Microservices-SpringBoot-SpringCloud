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
import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.dto.ModuleRequestDTO;
import tn.eps.scolarite.cours.dto.ModuleResponseDTO;
import tn.eps.scolarite.cours.exceptions.ApiError;
import tn.eps.scolarite.cours.services.ModuleService;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@Tag(name = "Modules (UE)", description = "Unités d'enseignement : coefficient, crédits ECTS, semestre")
public class ModuleRestController {

    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "Lister les modules")
    public List<ModuleResponseDTO> lister() {
        return moduleService.lister();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un module")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Module trouvé"),
            @ApiResponse(responseCode = "404", description = "Module introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ModuleResponseDTO trouver(@Parameter(example = "1") @PathVariable Long id) {
        return moduleService.trouverParId(id);
    }

    @GetMapping("/{id}/cours")
    @Operation(summary = "Lister les cours d'un module (relation @OneToMany)")
    public List<CoursResponseDTO> cours(@Parameter(example = "1") @PathVariable Long id) {
        return moduleService.coursDuModule(id);
    }

    @PostMapping
    @Operation(summary = "Créer un module")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Module créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Code déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ModuleResponseDTO> creer(@Valid @RequestBody ModuleRequestDTO dto) {
        ModuleResponseDTO cree = moduleService.creer(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un module")
    public ModuleResponseDTO modifier(@PathVariable Long id, @Valid @RequestBody ModuleRequestDTO dto) {
        return moduleService.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un module (et ses cours)")
    public void supprimer(@PathVariable Long id) {
        moduleService.supprimer(id);
    }
}
