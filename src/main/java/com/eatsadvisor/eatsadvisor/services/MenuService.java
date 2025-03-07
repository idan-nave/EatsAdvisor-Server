package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Dish;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private final OpenAIService openAIService;
    private final DishService dishService;
    private final RecommendationService recommendationService;

    public MenuService(
            OpenAIService openAIService,
            DishService dishService,
            RecommendationService recommendationService) {
        this.openAIService = openAIService;
        this.dishService = dishService;
        this.recommendationService = recommendationService;
    }

    /**
     * Process a menu image and extract text
     * @param menuImage The menu image file
     * @return The extracted menu text
     * @throws IOException If there's an error processing the image
     */
    public String processMenuImage(MultipartFile menuImage) throws IOException {
        if (menuImage == null || menuImage.isEmpty()) {
            throw new IllegalArgumentException("Menu image is required");
        }
        
        // Extract text from image using OpenAI
        return openAIService.extractTextFromImage(menuImage);
    }

    /**
     * Process menu text and extract dishes
     * @param menuText The menu text
     * @return List of extracted dishes
     */
    @Transactional
    public List<Dish> processMenuText(String menuText) {
        if (menuText == null || menuText.isEmpty()) {
            throw new IllegalArgumentException("Menu text is required");
        }
        
        // Process dishes from menu text
        return dishService.processDishesFromMenuText(menuText);
    }

    /**
     * Get recommendations for a menu
     * @param menuText The menu text
     * @param email The user's email (optional)
     * @return Map containing recommendations and other information
     */
    public Map<String, Object> getRecommendations(String menuText, String email) {
        if (menuText == null || menuText.isEmpty()) {
            throw new IllegalArgumentException("Menu text is required");
        }
        
        // Get recommendations from recommendation service
        return recommendationService.generateRecommendations(menuText, email);
    }

    /**
     * Get recommendations for a guest user
     * @param menuText The menu text
     * @param guestPreferences Map containing guest preferences
     * @return Map containing recommendations and other information
     */
    public Map<String, Object> getRecommendationsForGuest(String menuText, Map<String, Object> guestPreferences) {
        if (menuText == null || menuText.isEmpty()) {
            throw new IllegalArgumentException("Menu text is required");
        }
        
        // Get recommendations from recommendation service
        return recommendationService.generateRecommendationsForGuest(menuText, guestPreferences);
    }

    /**
     * Process a menu image and get recommendations
     * @param menuImage The menu image file
     * @param email The user's email (optional)
     * @return Map containing extracted text, recommendations, and other information
     * @throws IOException If there's an error processing the image
     */
    public Map<String, Object> processMenuImageAndGetRecommendations(MultipartFile menuImage, String email) throws IOException {
        // Extract text from image
        String menuText = processMenuImage(menuImage);
        
        // Process dishes from menu text
        List<Dish> dishes = processMenuText(menuText);
        
        // Get recommendations
        Map<String, Object> recommendations = getRecommendations(menuText, email);
        
        // Combine results
        Map<String, Object> result = new HashMap<>();
        result.put("menuText", menuText);
        result.put("dishes", dishes.stream()
                .map(dish -> {
                    Map<String, Object> dishMap = new HashMap<>();
                    dishMap.put("id", dish.getId());
                    dishMap.put("name", dish.getName());
                    dishMap.put("description", dish.getDescription() != null ? dish.getDescription() : "");
                    return dishMap;
                })
                .collect(Collectors.toList()));
        result.put("recommendations", recommendations);
        
        return result;
    }

    /**
     * Rate a recommended dish
     * @param email The user's email
     * @param dishId The dish ID
     * @param rating The rating (1-5)
     * @return Map containing the result of the rating
     */
    @Transactional
    public Map<String, Object> rateDish(String email, Integer dishId, Integer rating) {
        try {
            // Save rating
            recommendationService.saveRecommendationRating(email, dishId, rating);
            
            // Return success response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Rating saved successfully");
            
            return result;
        } catch (Exception e) {
            // Return error response
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            
            return result;
        }
    }
}
