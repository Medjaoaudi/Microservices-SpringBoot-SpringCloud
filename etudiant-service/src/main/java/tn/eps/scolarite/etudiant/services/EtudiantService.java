package tn.eps.scolarite.etudiant.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.eps.scolarite.etudiant.dto.EtudiantRequestDTO;
import tn.eps.scolarite.etudiant.dto.EtudiantResponseDTO;
import tn.eps.scolarite.etudiant.entities.Filiere;

import java.util.List;

public interface EtudiantService {

    Page<EtudiantResponseDTO> lister(Pageable pageable);

    EtudiantResponseDTO trouverParId(Long id);

    EtudiantResponseDTO trouverParMatricule(String matricule);

    List<EtudiantResponseDTO> rechercher(String nom, Filiere filiere, Integer niveau);

    EtudiantResponseDTO creer(EtudiantRequestDTO dto);

    EtudiantResponseDTO modifier(Long id, EtudiantRequestDTO dto);

    void supprimer(Long id);

    boolean existe(Long id);
}
