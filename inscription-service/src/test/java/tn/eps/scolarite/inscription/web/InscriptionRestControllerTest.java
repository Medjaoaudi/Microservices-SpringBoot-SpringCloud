package tn.eps.scolarite.inscription.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.eps.scolarite.inscription.dto.InscriptionRequestDTO;
import tn.eps.scolarite.inscription.dto.InscriptionResponseDTO;
import tn.eps.scolarite.inscription.entities.StatutInscription;
import tn.eps.scolarite.inscription.exceptions.CapaciteAtteinteException;
import tn.eps.scolarite.inscription.exceptions.ServicePartenaireIndisponibleException;
import tn.eps.scolarite.inscription.model.Etudiant;
import tn.eps.scolarite.inscription.services.InscriptionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InscriptionRestController.class)
class InscriptionRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InscriptionService inscriptionService;

    @Test
    void get_reponseDegradee_200() throws Exception {
        when(inscriptionService.trouverParId(1L)).thenReturn(new InscriptionResponseDTO(1L, 1L, 1L,
                LocalDate.of(2026, 9, 15), "2026-2027", StatutInscription.VALIDEE, 16.5,
                Etudiant.indisponible(1L), null,
                Map.of("ETUDIANT-SERVICE", "INDISPONIBLE", "COURS-SERVICE", "UP"),
                List.of("ETUDIANT-SERVICE indisponible : données étudiant non disponibles")));

        mockMvc.perform(get("/api/inscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etudiant.nom").value("INDISPONIBLE"))
                .andExpect(jsonPath("$.etudiant.sourceDonnees").value("INDISPONIBLE"))
                .andExpect(jsonPath("$.serviceStatus.ETUDIANT-SERVICE").value("INDISPONIBLE"))
                .andExpect(jsonPath("$.warnings[0]").exists());
    }

    @Test
    void post_partenaireIndisponible_503() throws Exception {
        when(inscriptionService.inscrire(any(InscriptionRequestDTO.class))).thenThrow(
                new ServicePartenaireIndisponibleException("ETUDIANT-SERVICE", "ETUDIANT-SERVICE est indisponible"));

        mockMvc.perform(post("/api/inscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":1,\"coursId\":1}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "10"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.details.service").value("ETUDIANT-SERVICE"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void post_coursComplet_409() throws Exception {
        when(inscriptionService.inscrire(any(InscriptionRequestDTO.class)))
                .thenThrow(new CapaciteAtteinteException("Le cours BIGD-TP est complet (3/3)"));

        mockMvc.perform(post("/api/inscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":8,\"coursId\":6}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Le cours BIGD-TP est complet (3/3)"));
    }

    @Test
    void post_invalide_400() throws Exception {
        mockMvc.perform(post("/api/inscriptions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"coursId\":1,\"anneeUniversitaire\":\"26-27\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.etudiantId").exists())
                .andExpect(jsonPath("$.details.anneeUniversitaire").exists());
    }

    @Test
    void note_horsBornes_400() throws Exception {
        mockMvc.perform(put("/api/inscriptions/1/note").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":21}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.note").value("La note maximale est 20"));
    }

    @Test
    void statut_inconnu_400() throws Exception {
        mockMvc.perform(put("/api/inscriptions/1/statut").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"EXCLUE\"}"))
                .andExpect(status().isBadRequest());
    }
}
