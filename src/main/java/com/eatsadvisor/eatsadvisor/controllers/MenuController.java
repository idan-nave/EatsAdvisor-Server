package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.OpenAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final OpenAIService openAIService;

    public MenuController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMenu(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Mock image text extraction
            String menuText = openAIService.extractTextFromImage(file);

            // Step 2: Get AI Recommendations (Mocked)
            List<String> recommendations = openAIService.getRecommendations(menuText);

            // Step 3: Return response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Menu uploaded successfully (OCR & AI are mocked)");
            response.put("extracted_menu_text", menuText);
            response.put("recommendations", recommendations);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process menu"));
        }
    }
}
