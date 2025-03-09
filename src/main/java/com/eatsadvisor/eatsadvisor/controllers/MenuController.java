package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.MenuService;
import com.eatsadvisor.eatsadvisor.services.RecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/upload")
    public ResponseEntity<JsonNode> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Since we have .anyRequest().authenticated() in SecurityConfig,
            // we can safely assume the request is authenticated at this point
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String email = jwt.getClaim("email");

            // Extract text (menu) from the uploaded image
            JsonNode extractedMenu = menuService.extractTextFromImage(file);

            // Check if the response contains an error
            if (extractedMenu.has("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(extractedMenu);
            }

            // Get user preferences if email is provided
            Map<String, Object> userPreferences = recommendationService.getUserPreferencesForRecommendation(email);

            // Classify the extracted menu items
            JsonNode categorizedDishes = menuService.classifyDishes(extractedMenu, userPreferences);

            // Check if classification returned an error
            if (categorizedDishes.has("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(categorizedDishes);
            }

            return ResponseEntity.ok(categorizedDishes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapper.createObjectNode().put("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
