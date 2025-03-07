package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.Flavor;
import com.eatsadvisor.eatsadvisor.services.FlavorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/flavors")
public class FlavorController {

    private final FlavorService flavorService;

    public FlavorController(FlavorService flavorService) {
        this.flavorService = flavorService;
    }

    /**
     * Get all flavors
     * @return List of all flavors
     */
    @GetMapping
    public ResponseEntity<List<Flavor>> getAllFlavors() {
        List<Flavor> flavors = flavorService.getAllFlavors();
        return ResponseEntity.ok(flavors);
    }

    /**
     * Get a flavor by ID
     * @param id The flavor ID
     * @return The flavor if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Flavor> getFlavorById(@PathVariable Integer id) {
        Optional<Flavor> flavorOpt = flavorService.getFlavorById(id);
        return flavorOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search flavors by name
     * @param name The name to search for
     * @return List of matching flavors
     */
    @GetMapping("/search")
    public ResponseEntity<List<Flavor>> searchFlavorsByName(@RequestParam String name) {
        List<Flavor> flavors = flavorService.searchFlavorsByName(name);
        return ResponseEntity.ok(flavors);
    }

    /**
     * Create a new flavor (admin only)
     * @param flavorData Map containing flavor data
     * @return The created flavor
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createFlavor(@RequestBody Map<String, String> flavorData) {
        try {
            String name = flavorData.get("name");
            String description = flavorData.get("description");
            
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Flavor name is required"));
            }
            
            Flavor flavor = flavorService.createFlavor(name, description);
            return ResponseEntity.ok(flavor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create flavor: " + e.getMessage()));
        }
    }

    /**
     * Update an existing flavor (admin only)
     * @param id The flavor ID
     * @param flavorData Map containing flavor data
     * @return The updated flavor
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateFlavor(
            @PathVariable Integer id,
            @RequestBody Map<String, String> flavorData) {
        try {
            String name = flavorData.get("name");
            String description = flavorData.get("description");
            
            Flavor flavor = flavorService.updateFlavor(id, name, description);
            return ResponseEntity.ok(flavor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update flavor: " + e.getMessage()));
        }
    }

    /**
     * Delete a flavor (admin only)
     * @param id The flavor ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteFlavor(@PathVariable Integer id) {
        try {
            flavorService.deleteFlavor(id);
            return ResponseEntity.ok(Map.of("message", "Flavor deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete flavor: " + e.getMessage()));
        }
    }

    /**
     * Get flavors for a specific profile
     * @param profileId The profile ID
     * @return List of flavors for the profile
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<Flavor>> getFlavorsByProfileId(@PathVariable Integer profileId) {
        List<Flavor> flavors = flavorService.getFlavorsByProfileId(profileId);
        return ResponseEntity.ok(flavors);
    }

    /**
     * Get flavors for a specific profile with a minimum preference level
     * @param profileId The profile ID
     * @param minPreferenceLevel The minimum preference level
     * @return List of flavors for the profile with the minimum preference level
     */
    @GetMapping("/profile/{profileId}/min-preference/{minPreferenceLevel}")
    public ResponseEntity<List<Flavor>> getFlavorsByProfileIdAndMinPreferenceLevel(
            @PathVariable Integer profileId,
            @PathVariable Integer minPreferenceLevel) {
        List<Flavor> flavors = flavorService.getFlavorsByProfileIdAndMinPreferenceLevel(profileId, minPreferenceLevel);
        return ResponseEntity.ok(flavors);
    }

    /**
     * Get the most popular flavors
     * @param limit The maximum number of flavors to return
     * @return List of flavors with their average preference levels
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Object[]>> getMostPopularFlavors(@RequestParam(defaultValue = "10") Integer limit) {
        List<Object[]> popularFlavors = flavorService.getMostPopularFlavors(limit);
        return ResponseEntity.ok(popularFlavors);
    }
}
