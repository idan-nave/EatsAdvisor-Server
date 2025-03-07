package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.ConstraintType;
import com.eatsadvisor.eatsadvisor.services.ConstraintTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/constraint-types")
public class ConstraintTypeController {

    private final ConstraintTypeService constraintTypeService;

    public ConstraintTypeController(ConstraintTypeService constraintTypeService) {
        this.constraintTypeService = constraintTypeService;
    }

    /**
     * Get all constraint types
     * @return List of all constraint types
     */
    @GetMapping
    public ResponseEntity<List<ConstraintType>> getAllConstraintTypes() {
        List<ConstraintType> constraintTypes = constraintTypeService.getAllConstraintTypes();
        return ResponseEntity.ok(constraintTypes);
    }

    /**
     * Get a constraint type by ID
     * @param id The constraint type ID
     * @return The constraint type if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConstraintType> getConstraintTypeById(@PathVariable Integer id) {
        Optional<ConstraintType> constraintTypeOpt = constraintTypeService.getConstraintTypeById(id);
        return constraintTypeOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search constraint types by name
     * @param name The name to search for
     * @return List of matching constraint types
     */
    @GetMapping("/search")
    public ResponseEntity<List<ConstraintType>> searchConstraintTypesByName(@RequestParam String name) {
        List<ConstraintType> constraintTypes = constraintTypeService.searchConstraintTypesByName(name);
        return ResponseEntity.ok(constraintTypes);
    }

    /**
     * Create a new constraint type (admin only)
     * @param constraintTypeData Map containing constraint type data
     * @return The created constraint type
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createConstraintType(@RequestBody Map<String, String> constraintTypeData) {
        try {
            String name = constraintTypeData.get("name");
            
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Constraint type name is required"));
            }
            
            ConstraintType constraintType = constraintTypeService.createConstraintType(name);
            return ResponseEntity.ok(constraintType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create constraint type: " + e.getMessage()));
        }
    }

    /**
     * Update an existing constraint type (admin only)
     * @param id The constraint type ID
     * @param constraintTypeData Map containing constraint type data
     * @return The updated constraint type
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateConstraintType(
            @PathVariable Integer id,
            @RequestBody Map<String, String> constraintTypeData) {
        try {
            String name = constraintTypeData.get("name");
            
            ConstraintType constraintType = constraintTypeService.updateConstraintType(id, name);
            return ResponseEntity.ok(constraintType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update constraint type: " + e.getMessage()));
        }
    }

    /**
     * Delete a constraint type (admin only)
     * @param id The constraint type ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteConstraintType(@PathVariable Integer id) {
        try {
            constraintTypeService.deleteConstraintType(id);
            return ResponseEntity.ok(Map.of("message", "Constraint type deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete constraint type: " + e.getMessage()));
        }
    }

    /**
     * Get constraint types for a specific profile
     * @param profileId The profile ID
     * @return List of constraint types for the profile
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<ConstraintType>> getConstraintTypesByProfileId(@PathVariable Integer profileId) {
        List<ConstraintType> constraintTypes = constraintTypeService.getConstraintTypesByProfileId(profileId);
        return ResponseEntity.ok(constraintTypes);
    }

    /**
     * Get the count of profiles with a specific constraint type
     * @param constraintTypeId The constraint type ID
     * @return The number of profiles with the constraint type
     */
    @GetMapping("/{constraintTypeId}/profile-count")
    public ResponseEntity<Map<String, Object>> getProfileCountByConstraintTypeId(@PathVariable Integer constraintTypeId) {
        long count = constraintTypeService.getProfileCountByConstraintTypeId(constraintTypeId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("constraintTypeId", constraintTypeId);
        response.put("profileCount", count);
        
        return ResponseEntity.ok(response);
    }
}
