package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.services.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    private Profile testProfile;
    private AppUser testUser;
    private List<Profile> testProfiles;
    private Map<String, Object> testPreferences;

    @BeforeEach
    void setUp() {
        // Configure MockMvc with security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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
    @WithMockUser(roles = "USER")
    void getAllProfiles_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/profile")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(profileService, never()).getAllProfiles();
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
    @WithMockUser(roles = "USER")
    void getProfileById_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/profile/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(profileService, never()).getProfileById(anyInt());
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
    @WithMockUser(roles = "USER")
    void updateProfile_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", "1");
        requestBody.put("email", "updated@example.com");

        // Act & Assert
        mockMvc.perform(post("/profile/update")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());

        verify(profileService, never()).updateProfile(anyInt(), anyString());
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

    @Test
    @WithMockUser(roles = "USER")
    void deleteProfile_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/profile/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(profileService, never()).deleteProfile(anyInt());
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
        
        when(profileService.getUserPreferencesForRecommendation(eq("test@example.com"))).thenThrow(new RuntimeException("Server error"));

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
        
        when(profileService.getAppUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(profileService.getProfileByEmail("test@example.com")).thenReturn(Optional.of(testProfile));
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Preferences updated successfully")));

        verify(profileService, times(1)).getAppUserByEmail("test@example.com");
        verify(profileService, times(1)).getProfileByEmail("test@example.com");
        // Note: The setUserPreferences method is commented out in the controller
        // verify(profileService, times(1)).setUserPreferences(eq("test@example.com"), any());
    }

    @Test
    void setPreferences_WithNewUser_ShouldCreateProfileAndReturnSuccess() throws Exception {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-id")
                .claim("email", "test@example.com")
                .build();
        
        when(profileService.getAppUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(profileService.getProfileByEmail("test@example.com")).thenReturn(Optional.empty());
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

        verify(profileService, times(1)).getAppUserByEmail("test@example.com");
        verify(profileService, times(1)).getProfileByEmail("test@example.com");
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
        
        when(profileService.getAppUserByEmail("test@example.com")).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User not found")));

        verify(profileService, times(1)).getAppUserByEmail("test@example.com");
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
        
        when(profileService.getAppUserByEmail("test@example.com")).thenThrow(new RuntimeException("Server error"));
        
        // Act & Assert
        mockMvc.perform(post("/profile/preferences")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPreferences)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", containsString("Failed to set preferences")));

        verify(profileService, times(1)).getAppUserByEmail("test@example.com");
    }
}
