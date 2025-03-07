package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import com.eatsadvisor.eatsadvisor.services.AllergyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AllergyController.class)
class AllergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AllergyService allergyService;

    @Test
    void getAllAllergies_ShouldReturnAllAllergies() throws Exception {
        // Arrange
        Allergy peanutAllergy = new Allergy();
        peanutAllergy.setId(1);
        peanutAllergy.setName("Peanuts");
        peanutAllergy.setDescription("Peanut allergy");

        Allergy glutenAllergy = new Allergy();
        glutenAllergy.setId(2);
        glutenAllergy.setName("Gluten");
        glutenAllergy.setDescription("Gluten intolerance");

        List<Allergy> allergies = Arrays.asList(peanutAllergy, glutenAllergy);
        when(allergyService.getAllAllergies()).thenReturn(allergies);

        // Act & Assert
        mockMvc.perform(get("/api/allergies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Peanuts"))
                .andExpect(jsonPath("$[1].name").value("Gluten"));
    }

    @Test
    void getAllergyById_WithValidId_ShouldReturnAllergy() throws Exception {
        // Arrange
        Allergy peanutAllergy = new Allergy();
        peanutAllergy.setId(1);
        peanutAllergy.setName("Peanuts");
        peanutAllergy.setDescription("Peanut allergy");

        when(allergyService.getAllergyById(1)).thenReturn(Optional.of(peanutAllergy));

        // Act & Assert
        mockMvc.perform(get("/api/allergies/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Peanuts"));
    }

    @Test
    void createAllergy_WithValidData_ShouldCreateAllergy() throws Exception {
        // Arrange
        Allergy peanutAllergy = new Allergy();
        peanutAllergy.setId(1);
        peanutAllergy.setName("Peanuts");
        peanutAllergy.setDescription("Peanut allergy");

        when(allergyService.createAllergy("Peanuts", "Peanut allergy")).thenReturn(peanutAllergy);

        // Act & Assert
        mockMvc.perform(post("/api/allergies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peanutAllergy)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Peanuts"));
    }

    @Test
    void updateAllergy_WithExistingId_ShouldUpdateAllergy() throws Exception {
        // Arrange
        Allergy peanutAllergy = new Allergy();
        peanutAllergy.setId(1);
        peanutAllergy.setName("Peanuts");
        peanutAllergy.setDescription("Peanut allergy");

        when(allergyService.updateAllergy(1, "Updated Peanuts", "Updated allergy")).thenReturn(peanutAllergy);

        // Act & Assert
        mockMvc.perform(put("/api/allergies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(peanutAllergy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Peanuts"));
    }

    @Test
    void deleteAllergy_WithValidId_ShouldDeleteAllergy() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/allergies/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
