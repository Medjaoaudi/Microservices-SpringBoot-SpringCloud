package tn.eps.scolarite.etudiant.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.eps.scolarite.etudiant.dto.EtudiantRequestDTO;
import tn.eps.scolarite.etudiant.dto.EtudiantResponseDTO;
import tn.eps.scolarite.etudiant.entities.Etudiant;
import tn.eps.scolarite.etudiant.entities.Filiere;
import tn.eps.scolarite.etudiant.exceptions.DuplicateResourceException;
import tn.eps.scolarite.etudiant.exceptions.ResourceNotFoundException;
import tn.eps.scolarite.etudiant.mappers.EtudiantMapper;
import tn.eps.scolarite.etudiant.repositories.EtudiantRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final EtudiantMapper etudiantMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EtudiantResponseDTO> lister(Pageable pageable) {
        return etudiantRepository.findAll(pageable).map(etudiantMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EtudiantResponseDTO trouverParId(Long id) {
        return etudiantMapper.toDto(chargerOuEchouer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EtudiantResponseDTO trouverParMatricule(String matricule) {
        return etudiantRepository.findByMatricule(matricule.trim().toUpperCase())
                .map(etudiantMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun étudiant avec le matricule " + matricule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtudiantResponseDTO> rechercher(String nom, Filiere filiere, Integer niveau) {
        String critereNom = nom == null ? "" : nom.trim();
        return etudiantRepository.rechercher(critereNom, filiere, niveau).stream()
                .map(etudiantMapper::toDto)
                .toList();
    }

    @Override
    public EtudiantResponseDTO creer(EtudiantRequestDTO dto) {
        Etudiant etudiant = etudiantMapper.toEntity(dto);
        if (etudiantRepository.existsByMatricule(etudiant.getMatricule())) {
            throw new DuplicateResourceException("Le matricule " + etudiant.getMatricule() + " existe déjà");
        }
        if (etudiantRepository.existsByEmail(etudiant.getEmail())) {
            throw new DuplicateResourceException("L'email " + etudiant.getEmail() + " est déjà utilisé");
        }
        Etudiant enregistre = etudiantRepository.save(etudiant);
        log.info("Étudiant créé : id={} matricule={}", enregistre.getId(), enregistre.getMatricule());
        return etudiantMapper.toDto(enregistre);
    }

    @Override
    public EtudiantResponseDTO modifier(Long id, EtudiantRequestDTO dto) {
        Etudiant etudiant = chargerOuEchouer(id);
        String matricule = dto.matricule().trim().toUpperCase();
        String email = dto.email().trim().toLowerCase();
        if (etudiantRepository.existsByMatriculeAndIdNot(matricule, id)) {
            throw new DuplicateResourceException("Le matricule " + matricule + " existe déjà");
        }
        if (etudiantRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("L'email " + email + " est déjà utilisé");
        }
        etudiantMapper.updateEntity(etudiant, dto);
        log.info("Étudiant modifié : id={}", id);
        return etudiantMapper.toDto(etudiantRepository.save(etudiant));
    }

    @Override
    public void supprimer(Long id) {
        Etudiant etudiant = chargerOuEchouer(id);
        etudiantRepository.delete(etudiant);
        log.info("Étudiant supprimé : id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return etudiantRepository.existsById(id);
    }

    private Etudiant chargerOuEchouer(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable (id=" + id + ")"));
    }
}
