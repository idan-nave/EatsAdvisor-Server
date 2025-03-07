package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.ConstraintType;
import com.eatsadvisor.eatsadvisor.repositories.ConstraintTypeRepository;
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
class ConstraintTypeServiceTest {

    @Mock
    private ConstraintTypeRepository constraintTypeRepository;

    @InjectMocks
    private ConstraintTypeService constraintTypeService;

    private ConstraintType dietaryRestriction;
    private ConstraintType cookingMethod;

    @BeforeEach
    void setUp() {
        dietaryRestriction = new ConstraintType();
        dietaryRestriction.setId(1);
        dietaryRestriction.setName("Vegetarian");
        
        cookingMethod = new ConstraintType();
        cookingMethod.setId(2);
        cookingMethod.setName("Gluten-Free");
    }

    @Test
    void getAllConstraintTypes_ShouldReturnAllConstraints() {
        when(constraintTypeRepository.findAll()).thenReturn(Arrays.asList(dietaryRestriction, cookingMethod));
        List<ConstraintType> result = constraintTypeService.getAllConstraintTypes();
        assertEquals(2, result.size());
        verify(constraintTypeRepository, times(1)).findAll();
    }

    @Test
    void getConstraintTypeById_WithValidId_ShouldReturnConstraint() {
        when(constraintTypeRepository.findById(1)).thenReturn(Optional.of(dietaryRestriction));
        Optional<ConstraintType> result = constraintTypeService.getConstraintTypeById(1);
        assertTrue(result.isPresent());
        assertEquals("Vegetarian", result.get().getName());
    }

    @Test
    void createConstraintType_WithValidData_ShouldSaveConstraint() {
        when(constraintTypeRepository.save(any(ConstraintType.class))).thenReturn(dietaryRestriction);
        ConstraintType result = constraintTypeService.createConstraintType("Vegetarian");
        assertNotNull(result);
        assertEquals("Vegetarian", result.getName());
        verify(constraintTypeRepository, times(1)).save(any(ConstraintType.class));
    }

    @Test
    void updateConstraintType_WithExistingId_ShouldUpdateConstraint() {
        when(constraintTypeRepository.findById(1)).thenReturn(Optional.of(dietaryRestriction));
        when(constraintTypeRepository.save(any(ConstraintType.class))).thenReturn(dietaryRestriction);
        ConstraintType result = constraintTypeService.updateConstraintType(1, "Updated Vegetarian");
        assertEquals("Updated Vegetarian", result.getName());
        verify(constraintTypeRepository, times(1)).findById(1);
        verify(constraintTypeRepository, times(1)).save(any(ConstraintType.class));
    }

    @Test
    void updateConstraintType_WithNonExistingId_ShouldThrow() {
        when(constraintTypeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                constraintTypeService.updateConstraintType(99, "Invalid"));
    }

    @Test
    void deleteConstraintType_WithValidId_ShouldDeleteConstraint() {
        when(constraintTypeRepository.existsById(1)).thenReturn(true);
        doNothing().when(constraintTypeRepository).deleteById(1);
        constraintTypeService.deleteConstraintType(1);
        verify(constraintTypeRepository, times(1)).deleteById(1);
    }
}
