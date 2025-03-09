package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.services.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Basic tests for ProfileController that focus on the core functionality
 * without testing security or complex scenarios.
 */
@WebMvcTest(controllers = ProfileController.class)
@ActiveProfiles("test")
class ProfileControllerBasicTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    private Profile testProfile;
    private AppUser testUser;
    private List<Profile> testProfiles;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUsername("testuser");
        testUser.setOauthProvider("google");
        testUser.setOauthProviderId("123456");
        testUser.setCreatedAt(Instant.now());

        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setUser(testUser);
        testProfile.setCreatedAt(Instant.now());

        Profile profile2 = new Profile();
        profile2.setId(2);
        AppUser user2 = new AppUser();
        user2.setId(2);
        user2.setEmail("user2@example.com");
        profile2.setUser(user2);
        profile2.setCreatedAt(Instant.now());

        testProfiles = Arrays.asList(testProfile, profile2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllProfiles_ShouldReturnAllProfiles() throws Exception {
        // Arrange
        when(profileService.getAllProfiles()).thenReturn(testProfiles);

        // Act & Assert
        mockMvc.perform(get("/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(profileService, times(1)).getAllProfiles();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfileById_WithValidId_ShouldReturnProfile() throws Exception {
        // Arrange
        when(profileService.getProfileById(1)).thenReturn(Optional.of(testProfile));

        // Act & Assert
        mockMvc.perform(get("/profile/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(profileService, times(1)).getProfileById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfileById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(profileService.getProfileById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/profile/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).getProfileById(999);
    }

    @Test
    @WithMockUser
    void getProfileByEmail_WithValidEmail_ShouldReturnProfile() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", "test@example.com");
        
        when(profileService.getProfileByEmail("test@example.com")).thenReturn(Optional.of(testProfile));

        // Act & Assert
        mockMvc.perform(post("/profile/get")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(profileService, times(1)).getProfileByEmail("test@example.com");
    }

    @Test
    @WithMockUser
    void getProfileByEmail_WithInvalidEmail_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", "nonexistent@example.com");
        
        when(profileService.getProfileByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/profile/get")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).getProfileByEmail("nonexistent@example.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProfile_WithValidData_ShouldReturnUpdatedProfile() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", "1");
        requestBody.put("email", "updated@example.com");
        
        when(profileService.updateProfile(1, "updated@example.com")).thenReturn(testProfile);

        // Act & Assert
        mockMvc.perform(post("/profile/update")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)));

        verify(profileService, times(1)).updateProfile(1, "updated@example.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProfile_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", "999");
        requestBody.put("email", "updated@example.com");
        
        when(profileService.updateProfile(999, "updated@example.com")).thenThrow(new RuntimeException("Profile not found"));

        // Act & Assert
        mockMvc.perform(post("/profile/update")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).updateProfile(999, "updated@example.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProfile_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", "1");
        requestBody.put("email", "invalid-email");
        
        when(profileService.updateProfile(1, "invalid-email")).thenThrow(new IllegalArgumentException("Invalid email"));

        // Act & Assert
        mockMvc.perform(post("/profile/update")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid email")));

        verify(profileService, times(1)).updateProfile(1, "invalid-email");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProfile_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(profileService).deleteProfile(1);

        // Act & Assert
        mockMvc.perform(delete("/profile/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Profile deleted successfully")));

        verify(profileService, times(1)).deleteProfile(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProfile_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Profile not found")).when(profileService).deleteProfile(999);

        // Act & Assert
        mockMvc.perform(delete("/profile/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).deleteProfile(999);
    }
}
