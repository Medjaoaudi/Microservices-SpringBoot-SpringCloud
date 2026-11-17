package tn.eps.scolarite.cours.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.eps.scolarite.cours.dto.CapaciteDTO;
import tn.eps.scolarite.cours.dto.CoursRequestDTO;
import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.entities.Cours;
import tn.eps.scolarite.cours.entities.ModuleEnseignement;
import tn.eps.scolarite.cours.exceptions.DuplicateResourceException;
import tn.eps.scolarite.cours.exceptions.ResourceNotFoundException;
import tn.eps.scolarite.cours.mappers.CatalogueMapper;
import tn.eps.scolarite.cours.repositories.CoursRepository;
import tn.eps.scolarite.cours.repositories.ModuleRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CoursServiceImpl implements CoursService {

    private final CoursRepository coursRepository;
    private final ModuleRepository moduleRepository;
    private final CatalogueMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CoursResponseDTO> lister() {
        return coursRepository.findAll(Sort.by("id")).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoursResponseDTO trouverParId(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursResponseDTO> parModule(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module introuvable (id=" + moduleId + ")");
        }
        return coursRepository.findByModuleIdOrderByCode(moduleId).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursResponseDTO> rechercher(String titre, String enseignant) {
        return coursRepository.rechercher(titre == null ? "" : titre.trim(),
                        enseignant == null ? "" : enseignant.trim())
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CapaciteDTO capacite(Long id) {
        Cours c = charger(id);
        return new CapaciteDTO(c.getId(), c.getCode(), c.getCapaciteMax());
    }

    @Override
    public CoursResponseDTO creer(CoursRequestDTO dto) {
        String code = dto.code().trim().toUpperCase();
        if (coursRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Le cours " + code + " existe déjà");
        }
        Cours cours = new Cours();
        mapper.updateEntity(cours, dto, chargerModule(dto.moduleId()));
        Cours enregistre = coursRepository.save(cours);
        log.info("Cours créé : {} ({})", enregistre.getCode(), enregistre.getId());
        return mapper.toDto(enregistre);
    }

    @Override
    public CoursResponseDTO modifier(Long id, CoursRequestDTO dto) {
        Cours cours = charger(id);
        String code = dto.code().trim().toUpperCase();
        if (coursRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Le cours " + code + " existe déjà");
        }
        mapper.updateEntity(cours, dto, chargerModule(dto.moduleId()));
        return mapper.toDto(coursRepository.save(cours));
    }

    @Override
    public void supprimer(Long id) {
        Cours cours = charger(id);
        cours.getModule().getCours().remove(cours);
        coursRepository.delete(cours);
        log.info("Cours supprimé : id={}", id);
    }

    private Cours charger(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable (id=" + id + ")"));
    }

    private ModuleEnseignement chargerModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module introuvable (id=" + moduleId + ")"));
    }
}
