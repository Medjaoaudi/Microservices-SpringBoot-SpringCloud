package tn.eps.scolarite.etudiant.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.eps.scolarite.etudiant.dto.EtudiantRequestDTO;
import tn.eps.scolarite.etudiant.dto.EtudiantResponseDTO;
import tn.eps.scolarite.etudiant.entities.Etudiant;
import tn.eps.scolarite.etudiant.entities.Filiere;
import tn.eps.scolarite.etudiant.exceptions.DuplicateResourceException;
import tn.eps.scolarite.etudiant.exceptions.ResourceNotFoundException;
import tn.eps.scolarite.etudiant.mappers.EtudiantMapper;
import tn.eps.scolarite.etudiant.repositories.EtudiantRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtudiantServiceImplTest {

    @Mock
    private EtudiantRepository repository;

    private EtudiantServiceImpl service;

    private final EtudiantRequestDTO demande = new EtudiantRequestDTO("etu-2026-050", "Karray", "Malek",
            "Malek.Karray@etudiant.eps.tn", "+216 22 345 678", LocalDate.of(2003, 4, 12), Filiere.GL, 5);

    @BeforeEach
    void setUp() {
        service = new EtudiantServiceImpl(repository, new EtudiantMapper());
    }

    @Test
    @DisplayName("Création nominale : matricule en majuscules, email en minuscules")
    void creer_ok() {
        when(repository.existsByMatricule("ETU-2026-050")).thenReturn(false);
        when(repository.existsByEmail("malek.karray@etudiant.eps.tn")).thenReturn(false);
        when(repository.save(any(Etudiant.class))).thenAnswer(inv -> {
            Etudiant e = inv.getArgument(0);
            e.setId(50L);
            return e;
        });

        EtudiantResponseDTO cree = service.creer(demande);

        assertThat(cree.id()).isEqualTo(50L);
        assertThat(cree.matricule()).isEqualTo("ETU-2026-050");
        assertThat(cree.email()).isEqualTo("malek.karray@etudiant.eps.tn");
    }

    @Test
    @DisplayName("Création refusée si le matricule existe déjà (409)")
    void creer_matriculeEnDouble() {
        when(repository.existsByMatricule("ETU-2026-050")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(demande))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ETU-2026-050");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Consultation d'un étudiant inexistant : ResourceNotFoundException")
    void trouver_introuvable() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Modification refusée si l'email appartient à un autre étudiant")
    void modifier_emailEnDouble() {
        when(repository.findById(1L)).thenReturn(Optional.of(Etudiant.builder().id(1L).build()));
        when(repository.existsByMatriculeAndIdNot("ETU-2026-050", 1L)).thenReturn(false);
        when(repository.existsByEmailAndIdNot("malek.karray@etudiant.eps.tn", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.modifier(1L, demande)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Recherche : un nom absent devient un filtre vide")
    void rechercher_nomNull() {
        when(repository.rechercher("", Filiere.GL, null)).thenReturn(List.of(
                Etudiant.builder().id(1L).matricule("ETU-2026-001").nom("Ben Salah").prenom("Amine")
                        .email("a@b.tn").filiere(Filiere.GL).niveau(5).build()));

        assertThat(service.rechercher(null, Filiere.GL, null)).hasSize(1);
    }
}
