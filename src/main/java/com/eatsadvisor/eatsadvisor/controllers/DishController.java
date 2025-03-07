package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.Dish;
import com.eatsadvisor.eatsadvisor.services.DishService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    /**
     * Get all dishes
     * @return List of all dishes
     */
    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes() {
        List<Dish> dishes = dishService.getAllDishes();
        return ResponseEntity.ok(dishes);
    }

    /**
     * Get a dish by ID
     * @param id The dish ID
     * @return The dish if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable Integer id) {
        Optional<Dish> dishOpt = dishService.getDishById(id);
        return dishOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search dishes by name
     * @param name The name to search for
     * @return List of matching dishes
     */
    @GetMapping("/search")
    public ResponseEntity<List<Dish>> searchDishesByName(@RequestParam String name) {
        List<Dish> dishes = dishService.searchDishesByName(name);
        return ResponseEntity.ok(dishes);
    }

    /**
     * Create a new dish (admin only)
     * @param dishData Map containing dish data
     * @return The created dish
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createDish(@RequestBody Map<String, String> dishData) {
        try {
            String name = dishData.get("name");
            String description = dishData.get("description");
            
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Dish name is required"));
            }
            
            Dish dish = dishService.createDish(name, description);
            return ResponseEntity.ok(dish);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create dish: " + e.getMessage()));
        }
    }

    /**
     * Update an existing dish (admin only)
     * @param id The dish ID
     * @param dishData Map containing dish data
     * @return The updated dish
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateDish(
            @PathVariable Integer id,
            @RequestBody Map<String, String> dishData) {
        try {
            String name = dishData.get("name");
            String description = dishData.get("description");
            
            Dish dish = dishService.updateDish(id, name, description);
            return ResponseEntity.ok(dish);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update dish: " + e.getMessage()));
        }
    }

    /**
     * Delete a dish (admin only)
     * @param id The dish ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteDish(@PathVariable Integer id) {
        try {
            dishService.deleteDish(id);
            return ResponseEntity.ok(Map.of("message", "Dish deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete dish: " + e.getMessage()));
        }
    }

    /**
     * Get dish history for a specific profile
     * @param profileId The profile ID
     * @return List of dish history entries for the profile
     */
    @GetMapping("/profile/{profileId}/history")
    public ResponseEntity<List<Dish>> getDishHistoryByProfileId(@PathVariable Integer profileId) {
        List<Dish> dishes = dishService.getDishesByProfileId(profileId);
        return ResponseEntity.ok(dishes);
    }
}
