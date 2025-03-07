package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import com.eatsadvisor.eatsadvisor.services.AllergyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/allergies")
public class AllergyController {

    private final AllergyService allergyService;

    public AllergyController(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    /**
     * Get all allergies
     * @return List of all allergies
     */
    @GetMapping
    public ResponseEntity<List<Allergy>> getAllAllergies() {
        List<Allergy> allergies = allergyService.getAllAllergies();
        return ResponseEntity.ok(allergies);
    }

    /**
     * Get an allergy by ID
     * @param id The allergy ID
     * @return The allergy if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Allergy> getAllergyById(@PathVariable Integer id) {
        Optional<Allergy> allergyOpt = allergyService.getAllergyById(id);
        return allergyOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search allergies by name
     * @param name The name to search for
     * @return List of matching allergies
     */
    @GetMapping("/search")
    public ResponseEntity<List<Allergy>> searchAllergiesByName(@RequestParam String name) {
        List<Allergy> allergies = allergyService.searchAllergiesByName(name);
        return ResponseEntity.ok(allergies);
    }

    /**
     * Create a new allergy (admin only)
     * @param allergyData Map containing allergy data
     * @return The created allergy
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createAllergy(@RequestBody Map<String, String> allergyData) {
        try {
            String name = allergyData.get("name");
            String description = allergyData.get("description");

            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Allergy name is required"));
            }

            Allergy allergy = allergyService.createAllergy(name, description);
            return ResponseEntity.ok(allergy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create allergy: " + e.getMessage()));
        }
    }

    /**
     * Update an existing allergy (admin only)
     * @param id The allergy ID
     * @param allergyData Map containing allergy data
     * @return The updated allergy
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateAllergy(
            @PathVariable Integer id,
            @RequestBody Map<String, String> allergyData) {
        try {
            String name = allergyData.get("name");
            String description = allergyData.get("description");

            Allergy allergy = allergyService.updateAllergy(id, name, description);
            return ResponseEntity.ok(allergy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update allergy: " + e.getMessage()));
        }
    }

    /**
     * Delete an allergy (admin only)
     * @param id The allergy ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteAllergy(@PathVariable Integer id) {
        try {
            allergyService.deleteAllergy(id);
            return ResponseEntity.ok(Map.of("message", "Allergy deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete allergy: " + e.getMessage()));
        }
    }

    /**
     * Get allergies for a specific profile
     * @param profileId The profile ID
     * @return List of allergies for the profile
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<Allergy>> getAllergiesByProfileId(@PathVariable Integer profileId) {
        List<Allergy> allergies = allergyService.getAllergiesByProfileId(profileId);
        return ResponseEntity.ok(allergies);
    }

    /**
     * Get the count of profiles with a specific allergy
     * @param allergyId The allergy ID
     * @return The number of profiles with the allergy
     */
    @GetMapping("/{allergyId}/profile-count")
    public ResponseEntity<Map<String, Object>> getProfileCountByAllergyId(@PathVariable Integer allergyId) {
        long count = allergyService.getProfileCountByAllergyId(allergyId);

        Map<String, Object> response = new HashMap<>();
        response.put("allergyId", allergyId);
        response.put("profileCount", count);

        return ResponseEntity.ok(response);
    }
}
