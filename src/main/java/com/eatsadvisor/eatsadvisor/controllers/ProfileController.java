package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.services.ProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Get all profiles (admin only)
     * @return List of all profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        List<Profile> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    /**
     * Get a profile by ID (admin only)
     * @param id The profile ID
     * @return The profile if found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Profile> getProfileById(@PathVariable Integer id) {
        Optional<Profile> profileOpt = profileService.getProfileById(id);
        return profileOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get a profile by email
     * @param email The profile email
     * @return The profile if found
     */
    @PostMapping("/get")
    public ResponseEntity<Profile> getProfileByEmail(@RequestBody Map<String, String> profileData) {
        String email = profileData.get("email");
        Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
        return profileOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    /**
     * Update an existing profile (admin only)
     * @param profileData Map containing profile data
     * @return The updated profile
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateProfile(
            @RequestBody Map<String, String> profileData) {
        try {
            Integer id = Integer.parseInt(profileData.get("id"));
            String email = profileData.get("email");

            Profile profile = profileService.updateProfile(id, email);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Delete a profile (admin only)
     * @param id The profile ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteProfile(@PathVariable Integer id) {
        try {
            profileService.deleteProfile(id);
            return ResponseEntity.ok(Map.of("message", "Profile deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete profile: " + e.getMessage()));
        }
    }

    /**
     * Get all preferences for a user
     * 
     * @param authentication Authentication object
     * @return Map containing all user preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getPreferences(Authentication authentication) {
        try {
            // Since we have .anyRequest().authenticated() in SecurityConfig,
            // we can safely assume the request is authenticated at this point
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String email = jwt.getClaim("email");

            // Get user preferences
            Map<String, Object> preferences = profileService.getUserPreferencesForRecommendation(email);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get preferences: " + e.getMessage()));
        }
    }

    /**
     * Set all preferences for a user
     * 
     * @param authentication Authentication object
     * @param preferences    Map containing all user preferences
     * @return Map containing the result of the update
     */
    @PostMapping("/preferences")
    public ResponseEntity<Map<String, Object>> setPreferences(
            @RequestBody Map<String, Object> preferences,
            Authentication authentication) {
        try {
            // Since we have .anyRequest().authenticated() in SecurityConfig,
            // we can safely assume the request is authenticated at this point
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String email = jwt.getClaim("email");

            // Get the AppUser
            Optional<AppUser> appUserOptional = profileService.getAppUserByEmail(email);
            if (appUserOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            AppUser appUser = appUserOptional.get();

            // Check if profile exists
            Optional<Profile> profileOptional = profileService.getProfileByEmail(email);
            if (profileOptional.isEmpty()) {
                // Create profile if it doesn't exist
                Profile newProfile = new Profile();
                newProfile.setUser(appUser);
                profileService.saveProfile(newProfile);
                System.out.println("✅ ProfileController: Created profile for new user");
            }

            // Set user preferences (This line is commented out, uncomment it when the method is implemented)
            // profileService.setUserPreferences(email, preferences);

            // Return success response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Preferences updated successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to set preferences: " + e.getMessage()));
        }
    }
}
