package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Generate personalized recommendations based on menu text
     * @param request Map containing menu text and user preferences
     * @param authentication Authentication object
     * @return Map containing recommendations and other information
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateRecommendations(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String menuText = (String) request.get("menuText");
            if (menuText == null || menuText.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Menu text is required"));
            }
            
            String email = null;
            
            // Get user email from authentication if available
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                email = jwt.getClaim("email");
            }
            
            // Generate recommendations
            Map<String, Object> recommendations;
            if (email != null) {
                // Authenticated user
                recommendations = recommendationService.generateRecommendations(menuText, email);
            } else {
                // Guest user
                Map<String, Object> guestPreferences = new HashMap<>();
                if (request.containsKey("preferences")) {
                    Object preferencesObj = request.get("preferences");
                    if (preferencesObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> prefMap = (Map<String, Object>) preferencesObj;
                        guestPreferences = prefMap;
                    }
                }
                recommendations = recommendationService.generateRecommendationsForGuest(menuText, guestPreferences);
            }
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to generate recommendations: " + e.getMessage()));
        }
    }

    /**
     * Save a user's rating for a recommended dish
     * @param request Map containing dishId and rating
     * @param authentication Authentication object
     * @return Map containing the result of the rating
     */
    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> rateDish(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            // Check if user is authenticated
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }
            
            // Get user email
            String email = jwt.getClaim("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid authentication"));
            }
            
            // Get request parameters
            Integer dishId = (Integer) request.get("dishId");
            Integer rating = (Integer) request.get("rating");
            
            if (dishId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Dish ID is required"));
            }
            
            if (rating == null || rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
            }
            
            // Save rating
            recommendationService.saveRecommendationRating(email, dishId, rating);
            
            // Return success response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Rating saved successfully");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save rating: " + e.getMessage()));
        }
    }

    /**
     * Get common allergies for recommendation UI
     * @return List of common allergies
     */
    @GetMapping("/allergies")
    public ResponseEntity<List<Map<String, Object>>> getCommonAllergies() {
        List<Map<String, Object>> allergies = recommendationService.getCommonAllergies();
        return ResponseEntity.ok(allergies);
    }

    /**
     * Get common flavors for recommendation UI
     * @return List of common flavors
     */
    @GetMapping("/flavors")
    public ResponseEntity<List<Map<String, Object>>> getCommonFlavors() {
        List<Map<String, Object>> flavors = recommendationService.getCommonFlavors();
        return ResponseEntity.ok(flavors);
    }

    /**
     * Get common dietary constraints for recommendation UI
     * @return List of common dietary constraints
     */
    @GetMapping("/constraints")
    public ResponseEntity<List<Map<String, Object>>> getCommonDietaryConstraints() {
        List<Map<String, Object>> constraints = recommendationService.getCommonDietaryConstraints();
        return ResponseEntity.ok(constraints);
    }

    /**
     * Get a user's dish history for the recommendation UI
     * @param authentication Authentication object
     * @return List of dish history entries
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getUserDishHistory(Authentication authentication) {
        try {
            // Check if user is authenticated
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                return ResponseEntity.status(401).body(null);
            }
            
            // Get user email
            String email = jwt.getClaim("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(401).body(null);
            }
            
            List<Map<String, Object>> dishHistory = recommendationService.getUserDishHistory(email);
            return ResponseEntity.ok(dishHistory);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
