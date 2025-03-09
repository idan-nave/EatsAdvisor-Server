package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.*;
import com.eatsadvisor.eatsadvisor.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileConstraintServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private ConstraintTypeRepository constraintTypeRepository;
    
    @Mock
    private ProfileConstraintRepository profileConstraintRepository;
    
    @InjectMocks
    private ProfileConstraintService profileConstraintService;
    
    private Profile testProfile;
    private ConstraintType testConstraintType;
    private ProfileConstraint testProfileConstraint;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setCreatedAt(Instant.now());
        
        // Set up test constraint type
        testConstraintType = new ConstraintType();
        testConstraintType.setId(1);
        testConstraintType.setName("Vegetarian");
        testConstraintType.setCreatedAt(Instant.now());
        
        // Set up test profile constraint
        testProfileConstraint = new ProfileConstraint();
        testProfileConstraint.setProfile(testProfile);
        testProfileConstraint.setConstraintType(testConstraintType);
        testProfileConstraint.setCreatedAt(Instant.now());
        
        // Mock repository methods
        when(profileRepository.findById(1)).thenReturn(Optional.of(testProfile));
        when(constraintTypeRepository.findById(1)).thenReturn(Optional.of(testConstraintType));
        when(profileConstraintRepository.save(any(ProfileConstraint.class))).thenReturn(testProfileConstraint);
    }
    
    @Test
    void testGetConstraintsByProfileId() {
        // Set up test data
        List<ConstraintType> constraints = Collections.singletonList(testConstraintType);
        when(constraintTypeRepository.findByProfileId(1)).thenReturn(constraints);
        
        // Execute the method
        List<ConstraintType> result = profileConstraintService.getConstraintsByProfileId(1);
        
        // Verify interactions
        verify(constraintTypeRepository, times(1)).findByProfileId(1);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testConstraintType.getId(), result.get(0).getId());
        assertEquals(testConstraintType.getName(), result.get(0).getName());
    }
    
    @Test
    void testAddConstraintToProfile() {
        // Set up test data
        when(profileConstraintRepository.findByProfileAndConstraintType(testProfile, testConstraintType))
                .thenReturn(Optional.empty());
        
        // Execute the method
        ProfileConstraint result = profileConstraintService.addConstraintToProfile(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(constraintTypeRepository, times(1)).findById(1);
        verify(profileConstraintRepository, times(1)).findByProfileAndConstraintType(testProfile, testConstraintType);
        verify(profileConstraintRepository, times(1)).save(any(ProfileConstraint.class));
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(testConstraintType, result.getConstraintType());
    }
    
    @Test
    void testAddConstraintToProfile_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileConstraintService.addConstraintToProfile(999, 1);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(constraintTypeRepository, never()).findById(anyInt());
        verify(profileConstraintRepository, never()).findByProfileAndConstraintType(any(), any());
        verify(profileConstraintRepository, never()).save(any());
    }
    
    @Test
    void testAddConstraintToProfile_ConstraintTypeNotFound() {
        // Set up test data
        when(constraintTypeRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileConstraintService.addConstraintToProfile(1, 999);
        });
        
        // Verify message
        assertEquals("Constraint type not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(constraintTypeRepository, times(1)).findById(999);
        verify(profileConstraintRepository, never()).findByProfileAndConstraintType(any(), any());
        verify(profileConstraintRepository, never()).save(any());
    }
    
    @Test
    void testAddConstraintToProfile_AlreadyExists() {
        // Set up test data
        when(profileConstraintRepository.findByProfileAndConstraintType(testProfile, testConstraintType))
                .thenReturn(Optional.of(testProfileConstraint));
        
        // Execute the method
        ProfileConstraint result = profileConstraintService.addConstraintToProfile(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(constraintTypeRepository, times(1)).findById(1);
        verify(profileConstraintRepository, times(1)).findByProfileAndConstraintType(testProfile, testConstraintType);
        verify(profileConstraintRepository, times(0)).save(any(ProfileConstraint.class));
        
        // Verify result
        assertEquals(testProfileConstraint, result);
    }
    
    @Test
    void testRemoveConstraintFromProfile() {
        // Execute the method
        profileConstraintService.removeConstraintFromProfile(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(constraintTypeRepository, times(1)).findById(1);
        verify(profileConstraintRepository, times(1)).deleteByProfileAndConstraintType(testProfile, testConstraintType);
    }
    
    @Test
    void testRemoveConstraintFromProfile_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileConstraintService.removeConstraintFromProfile(999, 1);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(constraintTypeRepository, never()).findById(anyInt());
        verify(profileConstraintRepository, never()).deleteByProfileAndConstraintType(any(), any());
    }
    
    @Test
    void testRemoveConstraintFromProfile_ConstraintTypeNotFound() {
        // Set up test data
        when(constraintTypeRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileConstraintService.removeConstraintFromProfile(1, 999);
        });
        
        // Verify message
        assertEquals("Constraint type not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(constraintTypeRepository, times(1)).findById(999);
        verify(profileConstraintRepository, never()).deleteByProfileAndConstraintType(any(), any());
    }
    
    @Test
    void testProcessConstraints() {
        // Set up test data
        List<String> constraints = Arrays.asList("Vegetarian", "Gluten-Free");
        List<ProfileConstraint> existingConstraints = Collections.singletonList(testProfileConstraint);
        
        when(profileConstraintRepository.findByProfile(testProfile)).thenReturn(existingConstraints);
        when(constraintTypeRepository.findByName("Vegetarian")).thenReturn(Optional.of(testConstraintType));
        when(constraintTypeRepository.findByName("Gluten-Free")).thenReturn(Optional.empty());
        
        ConstraintType glutenFreeConstraint = new ConstraintType();
        glutenFreeConstraint.setId(2);
        glutenFreeConstraint.setName("Gluten-Free");
        glutenFreeConstraint.setCreatedAt(Instant.now());
        when(constraintTypeRepository.save(any(ConstraintType.class))).thenReturn(glutenFreeConstraint);
        
        // Execute the method
        profileConstraintService.processConstraints(testProfile, constraints);
        
        // Verify interactions
        verify(profileConstraintRepository, times(1)).findByProfile(testProfile);
        verify(profileConstraintRepository, times(1)).deleteAll(existingConstraints);
        verify(constraintTypeRepository, times(2)).findByName(anyString());
        verify(constraintTypeRepository, times(1)).save(any(ConstraintType.class));
        verify(profileConstraintRepository, times(2)).save(any(ProfileConstraint.class));
    }
}
