package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.*;
import com.eatsadvisor.eatsadvisor.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService userService;
    private final ProfileService profileService;
    private final ProfileAllergyService profileAllergyService;
    private final ProfileFlavorPreferenceService profileFlavorPreferenceService;
    private final ProfileConstraintService profileConstraintService;
    private final SpecialPreferenceService specialPreferenceService;
    private final DishService dishService;

    public AppUserController(
            AppUserService userService, 
            ProfileService profileService,
            ProfileAllergyService profileAllergyService,
            ProfileFlavorPreferenceService profileFlavorPreferenceService,
            ProfileConstraintService profileConstraintService,
            SpecialPreferenceService specialPreferenceService,
            DishService dishService) {
        this.userService = userService;
        this.profileService = profileService;
        this.profileAllergyService = profileAllergyService;
        this.profileFlavorPreferenceService = profileFlavorPreferenceService;
        this.profileConstraintService = profileConstraintService;
        this.specialPreferenceService = specialPreferenceService;
        this.dishService = dishService;
    }

    /**
     * Get the authenticated user by extracting email from JWT
     */
    @GetMapping("/me")
    public ResponseEntity<AppUser> getAuthenticatedUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaim("email");
            Optional<AppUser> user = userService.findAppUserByEmail(email);
            return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
    }

    /**
     * Get user by email (admin-only feature)
     */
    @GetMapping("/{email}")
    public ResponseEntity<Optional<AppUser>> getUserByEmail(@PathVariable String email) {
        Optional<AppUser> user = userService.findAppUserByEmail(email);
        return user.isPresent() ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    /**
     * Update user preferences (e.g., dietary preferences)
     */
    @PostMapping("/update-preferences")
    public ResponseEntity<AppUser> updateUserPreferences(@RequestBody AppUser updatedUser) {
        AppUser savedUser = userService.updateUserPreferences(updatedUser);
        return ResponseEntity.ok(savedUser);
    }
    
    /**
     * Get user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaim("email");
            Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
            
            if (profileOpt.isEmpty()) {
                // Create profile if it doesn't exist
                Profile profile = profileService.createProfile(email);
                Map<String, Object> response = new HashMap<>();
                response.put("profile", profile);
                response.put("message", "Profile created successfully");
                return ResponseEntity.ok(response);
            }
            
            Profile profile = profileOpt.get();
            Integer profileId = profile.getId();
            
            // Get profile data
            List<Allergy> allergies = profileAllergyService.getAllergiesByProfileId(profileId);
            List<ProfileFlavorPreference> flavorPreferences = profileFlavorPreferenceService.getFlavorPreferencesByProfileId(profileId);
            List<ConstraintType> constraints = profileConstraintService.getConstraintsByProfileId(profileId);
            List<SpecialPreference> specialPreferences = specialPreferenceService.getSpecialPreferencesByProfileId(profileId);
            List<DishHistory> dishHistory = dishService.getDishHistoryByProfileId(profileId);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("profile", profile);
            response.put("allergies", allergies);
            response.put("flavorPreferences", flavorPreferences);
            response.put("constraints", constraints);
            response.put("specialPreferences", specialPreferences);
            response.put("dishHistory", dishHistory);
            
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
    }
    
    /**
     * Add allergy to profile
     */
    @PostMapping("/profile/allergies")
    public ResponseEntity<Map<String, Object>> addAllergyToProfile(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Profile profile = profileOpt.get();
                Integer allergyId = (Integer) request.get("allergyId");
                
                if (allergyId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Allergy ID is required"));
                }
                
                ProfileAllergy profileAllergy = profileAllergyService.addAllergyToProfile(profile.getId(), allergyId);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Allergy added to profile",
                    "profileAllergy", profileAllergy
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Remove allergy from profile
     */
    @DeleteMapping("/profile/allergies/{allergyId}")
    public ResponseEntity<Map<String, Object>> removeAllergyFromProfile(
            @PathVariable Integer allergyId,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Profile profile = profileOpt.get();
                
                profileAllergyService.removeAllergyFromProfile(profile.getId(), allergyId);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Allergy removed from profile"
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Set flavor preference
     */
    @PostMapping("/profile/flavor-preferences")
    public ResponseEntity<Map<String, Object>> setFlavorPreference(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Profile profile = profileOpt.get();
                Integer flavorId = (Integer) request.get("flavorId");
                Integer preferenceLevel = (Integer) request.get("preferenceLevel");
                
                if (flavorId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Flavor ID is required"));
                }
                
                if (preferenceLevel == null || preferenceLevel < 1 || preferenceLevel > 10) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Preference level must be between 1 and 10"));
                }
                
                ProfileFlavorPreference preference = profileFlavorPreferenceService.setFlavorPreference(profile.getId(), flavorId, preferenceLevel);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Flavor preference set",
                    "preference", preference
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add dietary constraint to profile
     */
    @PostMapping("/profile/constraints")
    public ResponseEntity<Map<String, Object>> addConstraintToProfile(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Profile profile = profileOpt.get();
                Integer constraintTypeId = (Integer) request.get("constraintTypeId");
                
                if (constraintTypeId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Constraint type ID is required"));
                }
                
                ProfileConstraint profileConstraint = profileConstraintService.addConstraintToProfile(profile.getId(), constraintTypeId);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Constraint added to profile",
                    "profileConstraint", profileConstraint
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Remove dietary constraint from profile
     */
    @DeleteMapping("/profile/constraints/{constraintTypeId}")
    public ResponseEntity<Map<String, Object>> removeConstraintFromProfile(
            @PathVariable Integer constraintTypeId,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Profile profile = profileOpt.get();
                
                profileConstraintService.removeConstraintFromProfile(profile.getId(), constraintTypeId);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Constraint removed from profile"
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add special preference to profile
     */
    @PostMapping("/profile/special-preferences")
    public ResponseEntity<Map<String, Object>> addSpecialPreference(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Profile profile = profileOpt.get();
                String description = (String) request.get("description");
                
                if (description == null || description.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Description is required"));
                }
                
                SpecialPreference specialPreference = specialPreferenceService.addSpecialPreference(profile.getId(), description);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Special preference added",
                    "specialPreference", specialPreference
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Delete special preference
     */
    @DeleteMapping("/profile/special-preferences/{specialPreferenceId}")
    public ResponseEntity<Map<String, Object>> deleteSpecialPreference(
            @PathVariable Integer specialPreferenceId,
            Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String email = jwt.getClaim("email");
                Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
                
                if (profileOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                specialPreferenceService.deleteSpecialPreference(specialPreferenceId);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Special preference deleted"
                ));
            }
            return ResponseEntity.status(401).build(); // Unauthorized if JWT is missing
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
