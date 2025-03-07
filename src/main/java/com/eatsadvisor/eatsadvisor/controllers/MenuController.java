package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.Dish;
import com.eatsadvisor.eatsadvisor.services.MenuService;
import com.eatsadvisor.eatsadvisor.services.OpenAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;
    private final OpenAIService openAIService;

    public MenuController(MenuService menuService, OpenAIService openAIService) {
        this.menuService = menuService;
        this.openAIService = openAIService;
    }

    /**
     * Upload a menu image and extract text
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMenu(@RequestParam("file") MultipartFile file) {
        try {
            // Extract text from menu image
            String menuText = menuService.processMenuImage(file);

            // Process dishes from menu text
            List<Dish> dishes = menuService.processMenuText(menuText);

            // Return response with extracted text and dishes
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Menu uploaded successfully");
            response.put("menuText", menuText);
            response.put("dishCount", dishes.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process menu: " + e.getMessage()));
        }
    }
    
    /**
     * Upload a menu image and get recommendations
     */
    @PostMapping("/upload-and-recommend")
    public ResponseEntity<Map<String, Object>> uploadMenuAndGetRecommendations(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String email = null;
            
            // Get user email from authentication if available
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                email = jwt.getClaim("email");
            }
            
            // Process menu image and get recommendations
            Map<String, Object> result = menuService.processMenuImageAndGetRecommendations(file, email);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process menu: " + e.getMessage()));
        }
    }
    
    /**
     * Get recommendations based on menu text
     */
    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(
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
            
            // Get recommendations
            Map<String, Object> recommendations;
            if (email != null) {
                // Authenticated user
                recommendations = menuService.getRecommendations(menuText, email);
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
                recommendations = menuService.getRecommendationsForGuest(menuText, guestPreferences);
            }
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get recommendations: " + e.getMessage()));
        }
    }
    
    /**
     * Rate a recommended dish
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
            Map<String, Object> result = menuService.rateDish(email, dishId, rating);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save rating: " + e.getMessage()));
        }
    }
    
    /**
     * Get mock recommendations (for testing)
     */
    @GetMapping("/mock-recommendations")
    public ResponseEntity<Map<String, Object>> getMockRecommendations(@RequestParam String menuText) {
        try {
            List<String> recommendations = openAIService.getMockRecommendations(menuText);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mock recommendations generated successfully");
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get mock recommendations"));
        }
    }
}
