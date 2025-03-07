package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Flavor;
import com.eatsadvisor.eatsadvisor.repositories.FlavorRepository;
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
class FlavorServiceTest {

    @Mock
    private FlavorRepository flavorRepository;

    @InjectMocks
    private FlavorService flavorService;

    private Flavor sweet;
    private Flavor sour;

    @BeforeEach
    void setUp() {
        sweet = new Flavor();
        sweet.setId(1);
        sweet.setName("Sweet");

        sour = new Flavor();
        sour.setId(2);
        sour.setName("Sour");
    }

    @Test
    void getAllFlavors_ShouldReturnAllFlavors() {
        when(flavorRepository.findAll()).thenReturn(Arrays.asList(sweet, sour));
        List<Flavor> result = flavorService.getAllFlavors();
        assertEquals(2, result.size());
        verify(flavorRepository, times(1)).findAll();
    }

    @Test
    void getFlavorById_WithValidId_ShouldReturnFlavor() {
        when(flavorRepository.findById(1)).thenReturn(Optional.of(sweet));
        Optional<Flavor> result = flavorService.getFlavorById(1);
        assertTrue(result.isPresent());
        assertEquals("Sweet", result.get().getName());
    }

    @Test
    void createFlavor_WithValidData_ShouldSaveFlavor() {
        when(flavorRepository.save(any(Flavor.class))).thenReturn(sweet);
        Flavor result = flavorService.createFlavor("Sweet", "Sweet description");
        assertNotNull(result);
        assertEquals("Sweet", result.getName());
        verify(flavorRepository, times(1)).save(any(Flavor.class));
    }

    @Test
    void updateFlavor_WithExistingId_ShouldUpdateFlavor() {
        when(flavorRepository.findById(1)).thenReturn(Optional.of(sweet));
        when(flavorRepository.save(any(Flavor.class))).thenReturn(sweet);
        Flavor result = flavorService.updateFlavor(1, "Updated Sweet", "Updated Sweet description");
        assertEquals("Updated Sweet", result.getName());
        verify(flavorRepository, times(1)).findById(1);
        verify(flavorRepository, times(1)).save(any(Flavor.class));
    }

    @Test
    void updateFlavor_WithNonExistingId_ShouldThrow() {
        when(flavorRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                flavorService.updateFlavor(99, "Invalid", "Invalid description"));
    }

    @Test
    void deleteFlavor_WithValidId_ShouldDeleteFlavor() {
        when(flavorRepository.existsById(1)).thenReturn(true);
        doNothing().when(flavorRepository).deleteById(1);
        flavorService.deleteFlavor(1);
        verify(flavorRepository, times(1)).deleteById(1);
    }
}
