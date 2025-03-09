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

class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private AppUserRepository appUserRepository;
    
    @Mock
    private RecommendationService recommendationService;
    
    @InjectMocks
    private ProfileService profileService;
    
    private AppUser testUser;
    private Profile testProfile;
    private String testEmail = "test@example.com";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test user
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail(testEmail);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setUser(testUser);
        testProfile.setCreatedAt(Instant.now());
        
        // Mock repository methods
        when(appUserRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.of(testProfile));
        when(profileRepository.findById(1)).thenReturn(Optional.of(testProfile));
    }
    
    @Test
    void testGetAllProfiles() {
        // Set up test data
        List<Profile> profiles = Arrays.asList(testProfile);
        when(profileRepository.findAll()).thenReturn(profiles);
        
        // Execute the method
        List<Profile> result = profileService.getAllProfiles();
        
        // Verify interactions
        verify(profileRepository, times(1)).findAll();
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testProfile.getId(), result.get(0).getId());
    }
    
    @Test
    void testGetProfileById() {
        // Execute the method
        Optional<Profile> result = profileService.getProfileById(1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        
        // Verify result
        assertTrue(result.isPresent());
        assertEquals(testProfile.getId(), result.get().getId());
    }
    
    @Test
    void testGetProfileByEmail() {
        // Execute the method
        Optional<Profile> result = profileService.getProfileByEmail(testEmail);
        
        // Verify interactions
        verify(appUserRepository, times(1)).findByEmail(testEmail);
        verify(profileRepository, times(1)).findByUser(testUser);
        
        // Verify result
        assertTrue(result.isPresent());
        assertEquals(testProfile.getId(), result.get().getId());
    }
    
    @Test
    void testCreateProfile() {
        // Set up test data
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        
        // Execute the method
        Profile result = profileService.createProfile(testEmail);
        
        // Verify interactions
        verify(appUserRepository, times(1)).findByEmail(testEmail);
        verify(profileRepository, times(1)).findByUser(testUser);
        verify(profileRepository, times(1)).save(any(Profile.class));
        
        // Verify result
        assertEquals(testProfile.getId(), result.getId());
    }
    
    @Test
    void testCreateProfile_UserNotFound() {
        // Set up test data
        when(appUserRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.createProfile("nonexistent@example.com");
        });
        
        // Verify message
        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        
        // Verify interactions
        verify(appUserRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(profileRepository, times(0)).findByUser(any());
        verify(profileRepository, times(0)).save(any());
    }
    
    @Test
    void testUpdateProfile() {
        // Set up test data
        String newEmail = "new@example.com";
        AppUser newUser = new AppUser();
        newUser.setId(2);
        newUser.setEmail(newEmail);
        when(appUserRepository.findByEmail(newEmail)).thenReturn(Optional.of(newUser));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);
        
        // Execute the method
        Profile result = profileService.updateProfile(1, newEmail);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(appUserRepository, times(1)).findByEmail(newEmail);
        verify(profileRepository, times(1)).save(any(Profile.class));
        
        // Verify result
        assertEquals(testProfile.getId(), result.getId());
        assertEquals(newUser, result.getUser());
    }
    
    @Test
    void testUpdateProfile_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.updateProfile(999, testEmail);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(appUserRepository, times(0)).findByEmail(any());
        verify(profileRepository, times(0)).save(any());
    }
    
    @Test
    void testUpdateProfile_UserNotFound() {
        // Set up test data
        String nonExistentEmail = "nonexistent@example.com";
        when(appUserRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.updateProfile(1, nonExistentEmail);
        });
        
        // Verify message
        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(appUserRepository, times(1)).findByEmail(nonExistentEmail);
        verify(profileRepository, times(0)).save(any());
    }
    
    @Test
    void testDeleteProfile() {
        // Execute the method
        profileService.deleteProfile(1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(profileRepository, times(1)).delete(testProfile);
    }
    
    @Test
    void testDeleteProfile_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            profileService.deleteProfile(999);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(profileRepository, never()).delete(any(Profile.class));
    }
    
    @Test
    void testGetUserPreferencesForRecommendation() {
        // Set up test data
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("test", "value");
        when(recommendationService.getUserPreferencesForRecommendation(testEmail)).thenReturn(preferences);
        
        // Execute the method
        Map<String, Object> result = profileService.getUserPreferencesForRecommendation(testEmail);
        
        // Verify interactions
        verify(recommendationService, times(1)).getUserPreferencesForRecommendation(testEmail);
        
        // Verify result
        assertEquals(preferences, result);
    }
    
    @Test
    void testSetUserPreferences() {
        // Set up test data
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("test", "value");
        
        // Execute the method
        profileService.setUserPreferences(testEmail, preferences);
        
        // Verify interactions
        verify(recommendationService, times(1)).setUserPreferences(testEmail, preferences);
    }
    
    @Test
    void testGetAppUserByEmail() {
        // Execute the method
        Optional<AppUser> result = profileService.getAppUserByEmail(testEmail);
        
        // Verify interactions
        verify(appUserRepository, times(1)).findByEmail(testEmail);
        
        // Verify result
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
    }
    
    @Test
    void testSaveProfile() {
        // Set up test data
        when(profileRepository.save(testProfile)).thenReturn(testProfile);
        
        // Execute the method
        profileService.saveProfile(testProfile);
        
        // Verify interactions
        verify(profileRepository, times(1)).save(testProfile);
    }
}
