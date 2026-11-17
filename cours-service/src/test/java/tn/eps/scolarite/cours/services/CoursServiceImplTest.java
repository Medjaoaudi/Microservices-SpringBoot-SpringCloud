package tn.eps.scolarite.cours.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.eps.scolarite.cours.dto.CapaciteDTO;
import tn.eps.scolarite.cours.dto.CoursRequestDTO;
import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.entities.Cours;
import tn.eps.scolarite.cours.entities.ModuleEnseignement;
import tn.eps.scolarite.cours.entities.Semestre;
import tn.eps.scolarite.cours.entities.TypeCours;
import tn.eps.scolarite.cours.exceptions.DuplicateResourceException;
import tn.eps.scolarite.cours.exceptions.ResourceNotFoundException;
import tn.eps.scolarite.cours.mappers.CatalogueMapper;
import tn.eps.scolarite.cours.repositories.CoursRepository;
import tn.eps.scolarite.cours.repositories.ModuleRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoursServiceImplTest {

    @Mock
    private CoursRepository coursRepository;
    @Mock
    private ModuleRepository moduleRepository;

    private CoursServiceImpl service;

    private final ModuleEnseignement jee = ModuleEnseignement.builder().id(1L).code("UE-JEE2")
            .intitule("JEE2").semestre(Semestre.S9).coefficient(3.0).credits(5).build();

    private final CoursRequestDTO demande = new CoursRequestDTO("jee2-td", "TD Gateway", "Routage",
            21, "Dr. Hela Ben Amor", 30, TypeCours.TD, 1L);

    @BeforeEach
    void setUp() {
        service = new CoursServiceImpl(coursRepository, moduleRepository, new CatalogueMapper());
    }

    @Test
    void creer_ok_rattacheAuModule() {
        when(coursRepository.existsByCode("JEE2-TD")).thenReturn(false);
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(jee));
        when(coursRepository.save(any(Cours.class))).thenAnswer(inv -> {
            Cours c = inv.getArgument(0);
            c.setId(11L);
            return c;
        });

        CoursResponseDTO cree = service.creer(demande);

        assertThat(cree.id()).isEqualTo(11L);
        assertThat(cree.code()).isEqualTo("JEE2-TD");
        assertThat(cree.module().code()).isEqualTo("UE-JEE2");
        assertThat(cree.module().coefficient()).isEqualTo(3.0);
    }

    @Test
    void creer_moduleInexistant_404() {
        when(coursRepository.existsByCode("JEE2-TD")).thenReturn(false);
        when(moduleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(demande)).isInstanceOf(ResourceNotFoundException.class);
        verify(coursRepository, never()).save(any());
    }

    @Test
    void creer_codeEnDouble_409() {
        when(coursRepository.existsByCode("JEE2-TD")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(demande)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void capacite_renvoieCapaciteMax() {
        when(coursRepository.findById(6L)).thenReturn(Optional.of(Cours.builder().id(6L).code("BIGD-TP")
                .capaciteMax(3).module(jee).build()));

        CapaciteDTO capacite = service.capacite(6L);

        assertThat(capacite.capaciteMax()).isEqualTo(3);
        assertThat(capacite.code()).isEqualTo("BIGD-TP");
    }
}
