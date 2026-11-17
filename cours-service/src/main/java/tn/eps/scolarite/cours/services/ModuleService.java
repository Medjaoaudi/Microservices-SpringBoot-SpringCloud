package tn.eps.scolarite.cours.services;

import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.dto.ModuleRequestDTO;
import tn.eps.scolarite.cours.dto.ModuleResponseDTO;

import java.util.List;

public interface ModuleService {

    List<ModuleResponseDTO> lister();

    ModuleResponseDTO trouverParId(Long id);

    List<CoursResponseDTO> coursDuModule(Long id);

    ModuleResponseDTO creer(ModuleRequestDTO dto);

    ModuleResponseDTO modifier(Long id, ModuleRequestDTO dto);

    void supprimer(Long id);
}
