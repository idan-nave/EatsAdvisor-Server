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

class ProfileAllergyServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private AllergyRepository allergyRepository;
    
    @Mock
    private ProfileAllergyRepository profileAllergyRepository;
    
    @InjectMocks
    private ProfileAllergyService profileAllergyService;
    
    private Profile testProfile;
    private Allergy testAllergy;
    private ProfileAllergy testProfileAllergy;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setCreatedAt(Instant.now());
        
        // Set up test allergy
        testAllergy = new Allergy();
        testAllergy.setId(1);
        testAllergy.setName("Peanuts");
        testAllergy.setCreatedAt(Instant.now());
        
        // Set up test profile allergy
        testProfileAllergy = new ProfileAllergy();
        testProfileAllergy.setProfile(testProfile);
        testProfileAllergy.setAllergy(testAllergy);
        testProfileAllergy.setCreatedAt(Instant.now());
        
        // Mock repository methods
        when(profileRepository.findById(1)).thenReturn(Optional.of(testProfile));
        when(allergyRepository.findById(1)).thenReturn(Optional.of(testAllergy));
        when(profileAllergyRepository.save(any(ProfileAllergy.class))).thenReturn(testProfileAllergy);
    }
    
    @Test
    void testGetAllergiesByProfileId() {
        // Set up test data
        List<Allergy> allergies = Collections.singletonList(testAllergy);
        when(allergyRepository.findByProfileId(1)).thenReturn(allergies);
        
        // Execute the method
        List<Allergy> result = profileAllergyService.getAllergiesByProfileId(1);
        
        // Verify interactions
        verify(allergyRepository, times(1)).findByProfileId(1);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testAllergy.getId(), result.get(0).getId());
        assertEquals(testAllergy.getName(), result.get(0).getName());
    }
    
    @Test
    void testAddAllergyToProfile() {
        // Set up test data
        when(profileAllergyRepository.findByProfileAndAllergy(testProfile, testAllergy))
                .thenReturn(Optional.empty());
        
        // Execute the method
        ProfileAllergy result = profileAllergyService.addAllergyToProfile(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(allergyRepository, times(1)).findById(1);
        verify(profileAllergyRepository, times(1)).findByProfileAndAllergy(testProfile, testAllergy);
        verify(profileAllergyRepository, times(1)).save(any(ProfileAllergy.class));
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(testAllergy, result.getAllergy());
    }
    
    @Test
    void testAddAllergyToProfile_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileAllergyService.addAllergyToProfile(999, 1);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(allergyRepository, never()).findById(anyInt());
        verify(profileAllergyRepository, never()).findByProfileAndAllergy(any(), any());
        verify(profileAllergyRepository, never()).save(any());
    }
    
    @Test
    void testAddAllergyToProfile_AllergyNotFound() {
        // Set up test data
        when(allergyRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileAllergyService.addAllergyToProfile(1, 999);
        });
        
        // Verify message
        assertEquals("Allergy not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(allergyRepository, times(1)).findById(999);
        verify(profileAllergyRepository, never()).findByProfileAndAllergy(any(), any());
        verify(profileAllergyRepository, never()).save(any());
    }
    
    @Test
    void testAddAllergyToProfile_AlreadyExists() {
        // Set up test data
        when(profileAllergyRepository.findByProfileAndAllergy(testProfile, testAllergy))
                .thenReturn(Optional.of(testProfileAllergy));
        
        // Execute the method
        ProfileAllergy result = profileAllergyService.addAllergyToProfile(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(allergyRepository, times(1)).findById(1);
        verify(profileAllergyRepository, times(1)).findByProfileAndAllergy(testProfile, testAllergy);
        verify(profileAllergyRepository, times(0)).save(any(ProfileAllergy.class));
        
        // Verify result
        assertEquals(testProfileAllergy, result);
    }
    
    @Test
    void testRemoveAllergyFromProfile() {
        // Execute the method
        profileAllergyService.removeAllergyFromProfile(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(allergyRepository, times(1)).findById(1);
        verify(profileAllergyRepository, times(1)).deleteByProfileAndAllergy(testProfile, testAllergy);
    }
    
    @Test
    void testRemoveAllergyFromProfile_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileAllergyService.removeAllergyFromProfile(999, 1);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(allergyRepository, never()).findById(anyInt());
        verify(profileAllergyRepository, never()).deleteByProfileAndAllergy(any(), any());
    }
    
    @Test
    void testRemoveAllergyFromProfile_AllergyNotFound() {
        // Set up test data
        when(allergyRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileAllergyService.removeAllergyFromProfile(1, 999);
        });
        
        // Verify message
        assertEquals("Allergy not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(allergyRepository, times(1)).findById(999);
        verify(profileAllergyRepository, never()).deleteByProfileAndAllergy(any(), any());
    }
    
    @Test
    void testProcessAllergies() {
        // Set up test data
        List<String> allergies = Arrays.asList("Peanuts", "Shellfish");
        List<ProfileAllergy> existingAllergies = Collections.singletonList(testProfileAllergy);
        
        when(profileAllergyRepository.findByProfile(testProfile)).thenReturn(existingAllergies);
        when(allergyRepository.findByName("Peanuts")).thenReturn(Optional.of(testAllergy));
        when(allergyRepository.findByName("Shellfish")).thenReturn(Optional.empty());
        
        Allergy shellfishAllergy = new Allergy();
        shellfishAllergy.setId(2);
        shellfishAllergy.setName("Shellfish");
        shellfishAllergy.setCreatedAt(Instant.now());
        when(allergyRepository.save(any(Allergy.class))).thenReturn(shellfishAllergy);
        
        // Execute the method
        profileAllergyService.processAllergies(testProfile, allergies);
        
        // Verify interactions
        verify(profileAllergyRepository, times(1)).findByProfile(testProfile);
        verify(profileAllergyRepository, times(1)).deleteAll(existingAllergies);
        verify(allergyRepository, times(2)).findByName(anyString());
        verify(allergyRepository, times(1)).save(any(Allergy.class));
        verify(profileAllergyRepository, times(2)).save(any(ProfileAllergy.class));
    }
}
