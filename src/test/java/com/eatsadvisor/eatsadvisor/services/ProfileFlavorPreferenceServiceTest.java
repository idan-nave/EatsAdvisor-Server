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

class ProfileFlavorPreferenceServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private FlavorRepository flavorRepository;
    
    @Mock
    private ProfileFlavorPreferenceRepository profileFlavorPreferenceRepository;
    
    @InjectMocks
    private ProfileFlavorPreferenceService profileFlavorPreferenceService;
    
    private Profile testProfile;
    private Flavor testFlavor;
    private ProfileFlavorPreference testProfileFlavorPreference;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setCreatedAt(Instant.now());
        
        // Set up test flavor
        testFlavor = new Flavor();
        testFlavor.setId(1);
        testFlavor.setName("Sweet");
        testFlavor.setCreatedAt(Instant.now());
        
        // Set up test profile flavor preference
        testProfileFlavorPreference = new ProfileFlavorPreference();
        testProfileFlavorPreference.setProfile(testProfile);
        testProfileFlavorPreference.setFlavor(testFlavor);
        testProfileFlavorPreference.setPreferenceLevel(8);
        testProfileFlavorPreference.setCreatedAt(Instant.now());
        
        // Mock repository methods
        when(profileRepository.findById(1)).thenReturn(Optional.of(testProfile));
        when(flavorRepository.findById(1)).thenReturn(Optional.of(testFlavor));
        when(profileFlavorPreferenceRepository.save(any(ProfileFlavorPreference.class))).thenReturn(testProfileFlavorPreference);
    }
    
    @Test
    void testGetFlavorPreferencesByProfileId() {
        // Set up test data
        List<ProfileFlavorPreference> preferences = Collections.singletonList(testProfileFlavorPreference);
        when(profileFlavorPreferenceRepository.findByProfile(testProfile)).thenReturn(preferences);
        
        // Execute the method
        List<ProfileFlavorPreference> result = profileFlavorPreferenceService.getFlavorPreferencesByProfileId(1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(profileFlavorPreferenceRepository, times(1)).findByProfile(testProfile);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testProfileFlavorPreference.getProfile(), result.get(0).getProfile());
        assertEquals(testProfileFlavorPreference.getFlavor(), result.get(0).getFlavor());
        assertEquals(testProfileFlavorPreference.getPreferenceLevel(), result.get(0).getPreferenceLevel());
    }
    
    @Test
    void testGetFlavorPreferencesByProfileId_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileFlavorPreferenceService.getFlavorPreferencesByProfileId(999);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(profileFlavorPreferenceRepository, never()).findByProfile(any());
    }
    
    @Test
    void testSetFlavorPreference_New() {
        // Set up test data
        when(profileFlavorPreferenceRepository.findByProfileAndFlavor(testProfile, testFlavor))
                .thenReturn(Optional.empty());
        
        // Execute the method
        ProfileFlavorPreference result = profileFlavorPreferenceService.setFlavorPreference(1, 1, 8);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(flavorRepository, times(1)).findById(1);
        verify(profileFlavorPreferenceRepository, times(1)).findByProfileAndFlavor(testProfile, testFlavor);
        verify(profileFlavorPreferenceRepository, times(1)).save(any(ProfileFlavorPreference.class));
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(testFlavor, result.getFlavor());
        assertEquals(8, result.getPreferenceLevel());
    }
    
    @Test
    void testSetFlavorPreference_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileFlavorPreferenceService.setFlavorPreference(999, 1, 8);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(flavorRepository, never()).findById(anyInt());
        verify(profileFlavorPreferenceRepository, never()).findByProfileAndFlavor(any(), any());
        verify(profileFlavorPreferenceRepository, never()).save(any());
    }
    
    @Test
    void testSetFlavorPreference_FlavorNotFound() {
        // Set up test data
        when(flavorRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileFlavorPreferenceService.setFlavorPreference(1, 999, 8);
        });
        
        // Verify message
        assertEquals("Flavor not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(flavorRepository, times(1)).findById(999);
        verify(profileFlavorPreferenceRepository, never()).findByProfileAndFlavor(any(), any());
        verify(profileFlavorPreferenceRepository, never()).save(any());
    }
    
    @Test
    void testSetFlavorPreference_Update() {
        // Set up test data
        when(profileFlavorPreferenceRepository.findByProfileAndFlavor(testProfile, testFlavor))
                .thenReturn(Optional.of(testProfileFlavorPreference));
        
        // Execute the method
        ProfileFlavorPreference result = profileFlavorPreferenceService.setFlavorPreference(1, 1, 9);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(flavorRepository, times(1)).findById(1);
        verify(profileFlavorPreferenceRepository, times(1)).findByProfileAndFlavor(testProfile, testFlavor);
        verify(profileFlavorPreferenceRepository, times(1)).save(testProfileFlavorPreference);
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(testFlavor, result.getFlavor());
        assertEquals(9, result.getPreferenceLevel());
    }
    
    @Test
    void testSetFlavorPreference_InvalidPreferenceLevel() {
        // Test with preference level < 1
        assertThrows(IllegalArgumentException.class, () -> {
            profileFlavorPreferenceService.setFlavorPreference(1, 1, 0);
        });
        
        // Test with preference level > 10
        assertThrows(IllegalArgumentException.class, () -> {
            profileFlavorPreferenceService.setFlavorPreference(1, 1, 11);
        });
    }
    
    @Test
    void testProcessFlavorPreferences() {
        // Set up test data
        Map<String, Integer> flavorPrefs = new HashMap<>();
        flavorPrefs.put("Sweet", 8);
        flavorPrefs.put("Spicy", 6);
        
        List<ProfileFlavorPreference> existingPreferences = Collections.singletonList(testProfileFlavorPreference);
        
        when(profileFlavorPreferenceRepository.findByProfile(testProfile)).thenReturn(existingPreferences);
        when(flavorRepository.findByName("Sweet")).thenReturn(Optional.of(testFlavor));
        when(flavorRepository.findByName("Spicy")).thenReturn(Optional.empty());
        
        Flavor spicyFlavor = new Flavor();
        spicyFlavor.setId(2);
        spicyFlavor.setName("Spicy");
        spicyFlavor.setCreatedAt(Instant.now());
        when(flavorRepository.save(any(Flavor.class))).thenReturn(spicyFlavor);
        
        // Execute the method
        profileFlavorPreferenceService.processFlavorPreferences(testProfile, flavorPrefs);
        
        // Verify interactions
        verify(profileFlavorPreferenceRepository, times(1)).findByProfile(testProfile);
        verify(profileFlavorPreferenceRepository, times(1)).deleteAll(existingPreferences);
        verify(flavorRepository, times(2)).findByName(anyString());
        verify(flavorRepository, times(1)).save(any(Flavor.class));
        verify(profileFlavorPreferenceRepository, times(2)).save(any(ProfileFlavorPreference.class));
    }
    
    @Test
    void testProcessFlavorPreferences_InvalidPreferenceLevel() {
        // Set up test data with invalid preference level
        Map<String, Integer> flavorPrefs = new HashMap<>();
        flavorPrefs.put("Sweet", 0); // Invalid: < 1
        flavorPrefs.put("Spicy", 11); // Invalid: > 10
        flavorPrefs.put("Salty", 5); // Valid
        
        List<ProfileFlavorPreference> existingPreferences = Collections.singletonList(testProfileFlavorPreference);
        
        when(profileFlavorPreferenceRepository.findByProfile(testProfile)).thenReturn(existingPreferences);
        when(flavorRepository.findByName("Salty")).thenReturn(Optional.empty());
        
        Flavor saltyFlavor = new Flavor();
        saltyFlavor.setId(3);
        saltyFlavor.setName("Salty");
        saltyFlavor.setCreatedAt(Instant.now());
        when(flavorRepository.save(any(Flavor.class))).thenReturn(saltyFlavor);
        
        // Execute the method
        profileFlavorPreferenceService.processFlavorPreferences(testProfile, flavorPrefs);
        
        // Verify interactions
        verify(profileFlavorPreferenceRepository, times(1)).findByProfile(testProfile);
        verify(profileFlavorPreferenceRepository, times(1)).deleteAll(existingPreferences);
        verify(flavorRepository, times(1)).findByName(anyString()); // Only for "Salty"
        verify(flavorRepository, times(1)).save(any(Flavor.class));
        verify(profileFlavorPreferenceRepository, times(1)).save(any(ProfileFlavorPreference.class)); // Only for "Salty"
    }
}
