package tn.eps.scolarite.cours.mappers;

import org.springframework.stereotype.Component;
import tn.eps.scolarite.cours.dto.*;
import tn.eps.scolarite.cours.entities.Cours;
import tn.eps.scolarite.cours.entities.ModuleEnseignement;

/** Conversions Entités <-> DTO du catalogue (modules et cours). */
@Component
public class CatalogueMapper {

    public ModuleEnseignement toEntity(ModuleRequestDTO dto) {
        ModuleEnseignement m = new ModuleEnseignement();
        updateEntity(m, dto);
        return m;
    }

    public void updateEntity(ModuleEnseignement m, ModuleRequestDTO dto) {
        m.setCode(dto.code().trim().toUpperCase());
        m.setIntitule(dto.intitule().trim());
        m.setSemestre(dto.semestre());
        m.setCoefficient(dto.coefficient());
        m.setCredits(dto.credits());
    }

    public ModuleResponseDTO toDto(ModuleEnseignement m) {
        return new ModuleResponseDTO(m.getId(), m.getCode(), m.getIntitule(), m.getSemestre(),
                m.getCoefficient(), m.getCredits(), m.getCours() == null ? 0 : m.getCours().size());
    }

    public ModuleResumeDTO toResume(ModuleEnseignement m) {
        return new ModuleResumeDTO(m.getId(), m.getCode(), m.getIntitule(), m.getSemestre(),
                m.getCoefficient(), m.getCredits());
    }

    /** Le module est résolu par le service (moduleId -> entité). */
    public void updateEntity(Cours c, CoursRequestDTO dto, ModuleEnseignement module) {
        c.setCode(dto.code().trim().toUpperCase());
        c.setTitre(dto.titre().trim());
        c.setDescription(dto.description());
        c.setVolumeHoraire(dto.volumeHoraire());
        c.setEnseignant(dto.enseignant().trim());
        c.setCapaciteMax(dto.capaciteMax());
        c.setType(dto.type());
        c.setModule(module);
    }

    public CoursResponseDTO toDto(Cours c) {
        return new CoursResponseDTO(c.getId(), c.getCode(), c.getTitre(), c.getDescription(),
                c.getVolumeHoraire(), c.getEnseignant(), c.getCapaciteMax(), c.getType(),
                c.getModule() == null ? null : toResume(c.getModule()));
    }
}
