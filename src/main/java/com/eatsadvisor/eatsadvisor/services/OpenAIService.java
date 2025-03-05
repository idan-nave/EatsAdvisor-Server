package com.eatsadvisor.eatsadvisor.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    // Constructor (No self-injection)
    public OpenAIService() {}

    /**
     * Mocks AI recommendation based on extracted menu text.
     * @param menuText The menu text extracted from image.
     * @return List of recommended dishes.
     */
    public List<String> getRecommendations(String menuText) {
        return List.of("Mock Dish 1", "Mock Dish 2", "Mock Dish 3");
    }

    /**
     * Mocks text extraction from image (OCR placeholder).
     * @param file Uploaded menu image.
     * @return Extracted menu text.
     */
    public String extractTextFromImage(MultipartFile file) {
        return "Mock extracted menu text from image";
    }
}
