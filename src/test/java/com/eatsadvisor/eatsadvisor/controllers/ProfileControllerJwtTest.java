package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.config.JwtTestConfig;
import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.services.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for JWT-related endpoints in ProfileController
 */
@WebMvcTest(controllers = ProfileController.class)
@ActiveProfiles("test")
@Import(JwtTestConfig.class)
class ProfileControllerJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    private Profile testProfile;
    private AppUser testUser;
    private Map<String, Object> testPreferences;

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

        // Setup test preferences
        testPreferences = new HashMap<>();
        Map<String, Integer> flavorPreferences = new HashMap<>();
        flavorPreferences.put("sweet", 7);
        flavorPreferences.put("salty", 5);
        testPreferences.put("flavorPreferences", flavorPreferences);
        testPreferences.put("allergies", Arrays.asList("Peanuts", "Shellfish"));
        testPreferences.put("dietaryConstraints", Arrays.asList("Vegetarian"));
        testPreferences.put("specialPreferences", Arrays.asList("No onions"));
    }

    @Test
    void getPreferences_WithValidAuthentication_ShouldReturnPreferences() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getUserPreferencesForRecommendation(eq("test@example.com"))).thenReturn(testPreferences);

        // Act & Assert
        mockMvc.perform(get("/profile/preferences")
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flavorPreferences.sweet", is(7)))
                .andExpect(jsonPath("$.allergies", hasSize(2)))
                .andExpect(jsonPath("$.dietaryConstraints", hasSize(1)))
                .andExpect(jsonPath("$.specialPreferences", hasSize(1)));

        verify(profileService, times(1)).getUserPreferencesForRecommendation(eq("test@example.com"));
    }

    @Test
    void getPreferences_WithServerError_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getUserPreferencesForRecommendation(eq("test@example.com")))
            .thenThrow(new RuntimeException("Server error"));

        // Act & Assert
        mockMvc.perform(get("/profile/preferences")
                .with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Failed to get preferences")));

        verify(profileService, times(1)).getUserPreferencesForRecommendation(eq("test@example.com"));
    }

    @Test
    void setPreferences_WithValidData_ShouldReturnSuccess() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getAppUserByEmail(eq("test@example.com"))).thenReturn(Optional.of(testUser));
        when(profileService.getProfileByEmail(eq("test@example.com"))).thenReturn(Optional.of(testProfile));
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Preferences updated successfully")));

        verify(profileService, times(1)).getAppUserByEmail(eq("test@example.com"));
        verify(profileService, times(1)).getProfileByEmail(eq("test@example.com"));
    }

    @Test
    void setPreferences_WithNewUser_ShouldCreateProfileAndReturnSuccess() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getAppUserByEmail(eq("test@example.com"))).thenReturn(Optional.of(testUser));
        when(profileService.getProfileByEmail(eq("test@example.com"))).thenReturn(Optional.empty());
        doNothing().when(profileService).saveProfile(any(Profile.class));
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Preferences updated successfully")));

        verify(profileService, times(1)).getAppUserByEmail(eq("test@example.com"));
        verify(profileService, times(1)).getProfileByEmail(eq("test@example.com"));
        verify(profileService, times(1)).saveProfile(any(Profile.class));
    }

    @Test
    void setPreferences_WithUserNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getAppUserByEmail(eq("test@example.com"))).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(profileService, times(1)).getAppUserByEmail(eq("test@example.com"));
        verify(profileService, never()).getProfileByEmail(anyString());
        verify(profileService, never()).saveProfile(any(Profile.class));
    }

    @Test
    void setPreferences_WithServerError_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getAppUserByEmail(eq("test@example.com")))
            .thenThrow(new RuntimeException("Server error"));
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Failed to set preferences")));

        verify(profileService, times(1)).getAppUserByEmail(eq("test@example.com"));
    }
}
