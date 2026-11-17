package tn.eps.scolarite.cours.services;

import tn.eps.scolarite.cours.dto.CapaciteDTO;
import tn.eps.scolarite.cours.dto.CoursRequestDTO;
import tn.eps.scolarite.cours.dto.CoursResponseDTO;

import java.util.List;

public interface CoursService {

    List<CoursResponseDTO> lister();

    CoursResponseDTO trouverParId(Long id);

    List<CoursResponseDTO> parModule(Long moduleId);

    List<CoursResponseDTO> rechercher(String titre, String enseignant);

    CapaciteDTO capacite(Long id);

    CoursResponseDTO creer(CoursRequestDTO dto);

    CoursResponseDTO modifier(Long id, CoursRequestDTO dto);

    void supprimer(Long id);
}
