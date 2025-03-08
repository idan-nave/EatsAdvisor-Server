package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.MenuService;
import com.eatsadvisor.eatsadvisor.services.ProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private ProfileService profileService;

    @PostMapping("/upload")
    public ResponseEntity<JsonNode> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "email", required = false) String email) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Extract text (menu) from the uploaded image
            JsonNode extractedMenu = menuService.extractTextFromImage(file);

            // Check if the response contains an error
            if (extractedMenu.has("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(extractedMenu);
            }

            // Get user preferences if email is provided
            Map<String, Object> userPreferences = null;
            if (email != null && !email.isEmpty()) {
                userPreferences = profileService.getUserPreferencesForRecommendation(email);
            }

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
