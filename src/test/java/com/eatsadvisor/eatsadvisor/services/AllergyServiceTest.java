package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import com.eatsadvisor.eatsadvisor.repositories.AllergyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergyServiceTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    private Allergy peanutAllergy;
    private Allergy glutenAllergy;

    @BeforeEach
    void setUp() {
        peanutAllergy = new Allergy();
        peanutAllergy.setId(1);
        peanutAllergy.setName("Peanuts");
        peanutAllergy.setDescription("Peanut allergy");

        glutenAllergy = new Allergy();
        glutenAllergy.setId(2);
        glutenAllergy.setName("Gluten");
        glutenAllergy.setDescription("Gluten intolerance");
    }

    @Test
    void getAllAllergies_ShouldReturnAllAllergies() {
        // Arrange
        when(allergyRepository.findAll()).thenReturn(Arrays.asList(peanutAllergy, glutenAllergy));

        // Act
        List<Allergy> result = allergyService.getAllAllergies();

        // Assert
        assertEquals(2, result.size());
        verify(allergyRepository, times(1)).findAll();
    }

    @Test
    void getAllergyById_WithValidId_ShouldReturnAllergy() {
        // Arrange
        when(allergyRepository.findById(1)).thenReturn(Optional.of(peanutAllergy));

        // Act
        Optional<Allergy> result = allergyService.getAllergyById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Peanuts", result.get().getName());
    }

    @Test
    void createAllergy_WithValidData_ShouldSaveAllergy() {
        // Arrange
        when(allergyRepository.save(any(Allergy.class))).thenReturn(peanutAllergy);

        // Act
        Allergy result = allergyService.createAllergy("Peanuts", "Peanut allergy");

        // Assert
        assertNotNull(result);
        assertEquals("Peanuts", result.getName());
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_WithExistingId_ShouldUpdateAllergy() {
        // Arrange
        when(allergyRepository.findById(1)).thenReturn(Optional.of(peanutAllergy));
        when(allergyRepository.save(any(Allergy.class))).thenReturn(peanutAllergy);

        // Act
        Allergy result = allergyService.updateAllergy(1, "Updated Peanuts", "Updated description");

        // Assert
        assertEquals("Updated Peanuts", result.getName());
        verify(allergyRepository, times(1)).findById(1);
        verify(allergyRepository, times(1)).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_WithNonExistingId_ShouldThrow() {
        // Arrange
        when(allergyRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                allergyService.updateAllergy(99, "Invalid", "Allergy"));
    }

    @Test
    void deleteAllergy_WithValidId_ShouldDeleteAllergy() {
        // Arrange
        when(allergyRepository.existsById(1)).thenReturn(true);
        doNothing().when(allergyRepository).deleteById(1);

        // Act
        allergyService.deleteAllergy(1);

        // Assert
        verify(allergyRepository, times(1)).deleteById(1);
    }
}
