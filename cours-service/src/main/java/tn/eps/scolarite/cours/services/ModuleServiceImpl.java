package tn.eps.scolarite.cours.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.dto.ModuleRequestDTO;
import tn.eps.scolarite.cours.dto.ModuleResponseDTO;
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
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final CoursRepository coursRepository;
    private final CatalogueMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ModuleResponseDTO> lister() {
        return moduleRepository.findAll(Sort.by("semestre", "code")).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleResponseDTO trouverParId(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursResponseDTO> coursDuModule(Long id) {
        charger(id);
        return coursRepository.findByModuleIdOrderByCode(id).stream().map(mapper::toDto).toList();
    }

    @Override
    public ModuleResponseDTO creer(ModuleRequestDTO dto) {
        ModuleEnseignement module = mapper.toEntity(dto);
        if (moduleRepository.existsByCode(module.getCode())) {
            throw new DuplicateResourceException("Le module " + module.getCode() + " existe déjà");
        }
        ModuleEnseignement enregistre = moduleRepository.save(module);
        log.info("Module créé : {} ({})", enregistre.getCode(), enregistre.getId());
        return mapper.toDto(enregistre);
    }

    @Override
    public ModuleResponseDTO modifier(Long id, ModuleRequestDTO dto) {
        ModuleEnseignement module = charger(id);
        String code = dto.code().trim().toUpperCase();
        if (moduleRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Le module " + code + " existe déjà");
        }
        mapper.updateEntity(module, dto);
        return mapper.toDto(moduleRepository.save(module));
    }

    @Override
    public void supprimer(Long id) {
        ModuleEnseignement module = charger(id);
        log.info("Suppression du module {} et de ses {} cours", module.getCode(), module.getCours().size());
        moduleRepository.delete(module);
    }

    private ModuleEnseignement charger(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module introuvable (id=" + id + ")"));
    }
}
