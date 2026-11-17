package tn.eps.scolarite.cours.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.eps.scolarite.cours.dto.CapaciteDTO;
import tn.eps.scolarite.cours.dto.CoursResponseDTO;
import tn.eps.scolarite.cours.dto.ModuleResumeDTO;
import tn.eps.scolarite.cours.entities.Semestre;
import tn.eps.scolarite.cours.entities.TypeCours;
import tn.eps.scolarite.cours.exceptions.ResourceNotFoundException;
import tn.eps.scolarite.cours.services.CoursService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoursRestController.class)
class CoursRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoursService coursService;

    @Test
    void getCours_avecModuleSansBoucle() throws Exception {
        when(coursService.trouverParId(1L)).thenReturn(new CoursResponseDTO(1L, "JEE2-C", "Microservices", null,
                21, "Dr. Hela Ben Amor", 40, TypeCours.COURS,
                new ModuleResumeDTO(1L, "UE-JEE2", "JEE2", Semestre.S9, 3.0, 5)));

        mockMvc.perform(get("/api/cours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.module.code").value("UE-JEE2"))
                .andExpect(jsonPath("$.module.cours").doesNotExist());
    }

    @Test
    void capacite_200() throws Exception {
        when(coursService.capacite(6L)).thenReturn(new CapaciteDTO(6L, "BIGD-TP", 3));

        mockMvc.perform(get("/api/cours/6/capacite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capaciteMax").value(3));
    }

    @Test
    void parModule_inconnu_404() throws Exception {
        when(coursService.parModule(42L)).thenThrow(new ResourceNotFoundException("Module introuvable (id=42)"));

        mockMvc.perform(get("/api/cours/module/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Module introuvable (id=42)"));
    }

    @Test
    void recherche_200() throws Exception {
        when(coursService.rechercher("spring", null)).thenReturn(List.of());

        mockMvc.perform(get("/api/cours/search").param("titre", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void post_invalide_400() throws Exception {
        mockMvc.perform(post("/api/cours").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"titre\":\"T\",\"volumeHoraire\":0,\"enseignant\":\"X\",\"capaciteMax\":10,\"type\":\"TP\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.code").exists())
                .andExpect(jsonPath("$.details.volumeHoraire").exists())
                .andExpect(jsonPath("$.details.moduleId").exists());
    }
}
