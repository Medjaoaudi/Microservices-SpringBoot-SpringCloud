package tn.eps.scolarite.etudiant.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.eps.scolarite.etudiant.dto.EtudiantRequestDTO;
import tn.eps.scolarite.etudiant.dto.EtudiantResponseDTO;
import tn.eps.scolarite.etudiant.entities.Filiere;
import tn.eps.scolarite.etudiant.exceptions.ResourceNotFoundException;
import tn.eps.scolarite.etudiant.services.EtudiantService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EtudiantRestController.class)
class EtudiantRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EtudiantService etudiantService;

    private final EtudiantResponseDTO amine = new EtudiantResponseDTO(1L, "ETU-2026-001", "Ben Salah", "Amine",
            "amine.bensalah@etudiant.eps.tn", "+216 22 145 301", LocalDate.of(2002, 3, 14), Filiere.GL, 5,
            LocalDateTime.now());

    @Test
    void getParId_200() throws Exception {
        when(etudiantService.trouverParId(1L)).thenReturn(amine);

        mockMvc.perform(get("/api/etudiants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value("ETU-2026-001"))
                .andExpect(jsonPath("$.filiere").value("GL"));
    }

    @Test
    void getParId_404_formatUniforme() throws Exception {
        when(etudiantService.trouverParId(99L)).thenThrow(new ResourceNotFoundException("Étudiant introuvable (id=99)"));

        mockMvc.perform(get("/api/etudiants/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Étudiant introuvable (id=99)"))
                .andExpect(jsonPath("$.path").value("/api/etudiants/99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void post_invalide_400() throws Exception {
        String json = """
                {"matricule":"X","nom":"","prenom":"Malek","email":"pas-un-email","filiere":"GL","niveau":7}
                """;
        mockMvc.perform(post("/api/etudiants").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.matricule").exists())
                .andExpect(jsonPath("$.details.email").value("Email invalide"))
                .andExpect(jsonPath("$.details.niveau").value("Le niveau maximum est 5"));
        verify(etudiantService, never()).creer(any());
    }

    @Test
    void post_valide_201() throws Exception {
        when(etudiantService.creer(any(EtudiantRequestDTO.class))).thenReturn(amine);
        String json = """
                {"matricule":"ETU-2026-001","nom":"Ben Salah","prenom":"Amine","email":"amine.bensalah@etudiant.eps.tn",
                 "telephone":"+216 22 145 301","dateNaissance":"2002-03-14","filiere":"GL","niveau":5}
                """;
        mockMvc.perform(post("/api/etudiants").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/etudiants/1")))
                .andExpect(jsonPath("$.id").value(1));
    }
}
