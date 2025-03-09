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

class SpecialPreferenceServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private SpecialPreferenceRepository specialPreferenceRepository;
    
    @InjectMocks
    private SpecialPreferenceService specialPreferenceService;
    
    private Profile testProfile;
    private SpecialPreference testSpecialPreference;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setCreatedAt(Instant.now());
        
        // Set up test special preference
        testSpecialPreference = new SpecialPreference();
        testSpecialPreference.setId(1);
        testSpecialPreference.setProfile(testProfile);
        testSpecialPreference.setDescription("I prefer organic ingredients");
        testSpecialPreference.setCreatedAt(Instant.now());
        
        // Mock repository methods
        when(profileRepository.findById(1)).thenReturn(Optional.of(testProfile));
        when(specialPreferenceRepository.save(any(SpecialPreference.class))).thenReturn(testSpecialPreference);
    }
    
    @Test
    void testGetSpecialPreferencesByProfileId() {
        // Set up test data
        List<SpecialPreference> specialPreferences = Collections.singletonList(testSpecialPreference);
        when(specialPreferenceRepository.findByProfileId(1)).thenReturn(specialPreferences);
        
        // Execute the method
        List<SpecialPreference> result = specialPreferenceService.getSpecialPreferencesByProfileId(1);
        
        // Verify interactions
        verify(specialPreferenceRepository, times(1)).findByProfileId(1);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testSpecialPreference.getId(), result.get(0).getId());
        assertEquals(testSpecialPreference.getProfile(), result.get(0).getProfile());
        assertEquals(testSpecialPreference.getDescription(), result.get(0).getDescription());
    }
    
    @Test
    void testAddSpecialPreference() {
        // Set up test data
        String description = "I prefer organic ingredients";
        
        // Execute the method
        SpecialPreference result = specialPreferenceService.addSpecialPreference(1, description);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(specialPreferenceRepository, times(1)).save(any(SpecialPreference.class));
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(description, result.getDescription());
    }
    
    @Test
    void testAddSpecialPreference_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        String description = "I prefer organic ingredients";
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            specialPreferenceService.addSpecialPreference(999, description);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(specialPreferenceRepository, never()).save(any());
    }
    
    @Test
    void testDeleteSpecialPreference() {
        // Execute the method
        specialPreferenceService.deleteSpecialPreference(1);
        
        // Verify interactions
        verify(specialPreferenceRepository, times(1)).deleteById(1);
    }
    
    @Test
    void testDeleteSpecialPreference_NotFound() {
        // Set up test data to simulate the repository throwing an exception when the entity is not found
        doThrow(new RuntimeException("Special preference not found with ID: 999"))
            .when(specialPreferenceRepository).deleteById(999);
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            specialPreferenceService.deleteSpecialPreference(999);
        });
        
        // Verify message
        assertEquals("Special preference not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(specialPreferenceRepository, times(1)).deleteById(999);
    }
    
    @Test
    void testProcessSpecialPreferences() {
        // Set up test data
        List<String> specialPrefs = Arrays.asList(
            "I prefer organic ingredients",
            "I avoid processed foods"
        );
        
        List<SpecialPreference> existingPreferences = Collections.singletonList(testSpecialPreference);
        when(specialPreferenceRepository.findByProfile(testProfile)).thenReturn(existingPreferences);
        
        // Execute the method
        specialPreferenceService.processSpecialPreferences(testProfile, specialPrefs);
        
        // Verify interactions
        verify(specialPreferenceRepository, times(1)).findByProfile(testProfile);
        verify(specialPreferenceRepository, times(1)).deleteAll(existingPreferences);
        verify(specialPreferenceRepository, times(2)).save(any(SpecialPreference.class));
    }
    
    @Test
    void testProcessSpecialPreferences_EmptyOrNull() {
        // Set up test data with empty and null descriptions
        List<String> specialPrefs = Arrays.asList(
            "",
            null,
            "I prefer organic ingredients"
        );
        
        List<SpecialPreference> existingPreferences = Collections.singletonList(testSpecialPreference);
        when(specialPreferenceRepository.findByProfile(testProfile)).thenReturn(existingPreferences);
        
        // Execute the method
        specialPreferenceService.processSpecialPreferences(testProfile, specialPrefs);
        
        // Verify interactions
        verify(specialPreferenceRepository, times(1)).findByProfile(testProfile);
        verify(specialPreferenceRepository, times(1)).deleteAll(existingPreferences);
        verify(specialPreferenceRepository, times(1)).save(any(SpecialPreference.class)); // Only for the non-empty description
    }
}
