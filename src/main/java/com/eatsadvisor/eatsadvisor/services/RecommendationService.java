package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.*;
import com.eatsadvisor.eatsadvisor.repositories.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final ProfileService profileService;
    private final DishService dishService;
    private final OpenAIService openAIService;
    private final AllergyRepository allergyRepository;
    private final FlavorRepository flavorRepository;
    private final ConstraintTypeRepository constraintTypeRepository;
    private final DishHistoryRepository dishHistoryRepository;

    public RecommendationService(
            ProfileService profileService,
            DishService dishService,
            OpenAIService openAIService,
            AllergyRepository allergyRepository,
            FlavorRepository flavorRepository,
            ConstraintTypeRepository constraintTypeRepository,
            SpecialPreferenceRepository specialPreferenceRepository,
            DishHistoryRepository dishHistoryRepository) {
        this.profileService = profileService;
        this.dishService = dishService;
        this.openAIService = openAIService;
        this.allergyRepository = allergyRepository;
        this.flavorRepository = flavorRepository;
        this.constraintTypeRepository = constraintTypeRepository;
        this.dishHistoryRepository = dishHistoryRepository;
    }

    /**
     * Generate personalized recommendations based on menu text and user preferences
     * @param menuText The menu text extracted from image
     * @param email The user's email (optional)
     * @return Map containing recommendations and other information
     */
    public Map<String, Object> generateRecommendations(String menuText, String email) {
        // Get user preferences if email is provided
        Map<String, Object> userPreferences = new HashMap<>();
        if (email != null && !email.isEmpty()) {
            userPreferences = profileService.getUserPreferencesForRecommendation(email);
        }
        
        // Get recommendations from OpenAI
        Map<String, Object> recommendations = openAIService.getRecommendations(menuText, userPreferences);
        
        // Process dishes from menu text
        List<Dish> dishes = dishService.processDishesFromMenuText(menuText);
        
        // Add dishes to response
        recommendations.put("dishes", dishes.stream()
                .map(dish -> {
                    Map<String, Object> dishMap = new HashMap<>();
                    dishMap.put("id", dish.getId());
                    dishMap.put("name", dish.getName());
                    dishMap.put("description", dish.getDescription() != null ? dish.getDescription() : "");
                    return dishMap;
                })
                .collect(Collectors.toList()));
        
        return recommendations;
    }

    /**
     * Generate recommendations for a guest user (no profile)
     * @param menuText The menu text extracted from image
     * @param guestPreferences Map containing guest preferences
     * @return Map containing recommendations and other information
     */
    public Map<String, Object> generateRecommendationsForGuest(String menuText, Map<String, Object> guestPreferences) {
        // Get recommendations from OpenAI
        Map<String, Object> recommendations = openAIService.getRecommendations(menuText, guestPreferences);
        
        // Process dishes from menu text
        List<Dish> dishes = dishService.processDishesFromMenuText(menuText);
        
        // Add dishes to response
        recommendations.put("dishes", dishes.stream()
                .map(dish -> {
                    Map<String, Object> dishMap = new HashMap<>();
                    dishMap.put("id", dish.getId());
                    dishMap.put("name", dish.getName());
                    dishMap.put("description", dish.getDescription() != null ? dish.getDescription() : "");
                    return dishMap;
                })
                .collect(Collectors.toList()));
        
        return recommendations;
    }

    /**
     * Get user preferences for recommendation
     * @param email The user's email
     * @return Map containing user preferences
     */
    public Map<String, Object> getUserPreferencesForRecommendation(String email) {
        return profileService.getUserPreferencesForRecommendation(email);
    }

    /**
     * Save a user's rating for a recommended dish
     * @param email The user's email
     * @param dishId The dish ID
     * @param rating The rating (1-5)
     * @return The created or updated dish history entry
     */
    public DishHistory saveRecommendationRating(String email, Integer dishId, Integer rating) {
        Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Profile not found for email: " + email);
        }
        
        Profile profile = profileOpt.get();
        return dishService.rateDish(profile.getId(), dishId, rating);
    }

    /**
     * Get common allergies for recommendation UI
     * @return List of common allergies
     */
    public List<Map<String, Object>> getCommonAllergies() {
        return allergyRepository.findAll().stream()
                .map(allergy -> {
                    Map<String, Object> allergyMap = new HashMap<>();
                    allergyMap.put("id", allergy.getId());
                    allergyMap.put("name", allergy.getName());
                    allergyMap.put("description", allergy.getDescription() != null ? allergy.getDescription() : "");
                    return allergyMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get common flavors for recommendation UI
     * @return List of common flavors
     */
    public List<Map<String, Object>> getCommonFlavors() {
        return flavorRepository.findAll().stream()
                .map(flavor -> {
                    Map<String, Object> flavorMap = new HashMap<>();
                    flavorMap.put("id", flavor.getId());
                    flavorMap.put("name", flavor.getName());
                    flavorMap.put("description", flavor.getDescription() != null ? flavor.getDescription() : "");
                    return flavorMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get common dietary constraints for recommendation UI
     * @return List of common dietary constraints
     */
    public List<Map<String, Object>> getCommonDietaryConstraints() {
        return constraintTypeRepository.findAll().stream()
                .map(constraintType -> {
                    Map<String, Object> constraintMap = new HashMap<>();
                    constraintMap.put("id", constraintType.getId());
                    constraintMap.put("name", constraintType.getName());
                    return constraintMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a user's dish history for the recommendation UI
     * @param email The user's email
     * @return List of dish history entries
     */
    public List<Map<String, Object>> getUserDishHistory(String email) {
        Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
        if (profileOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        Profile profile = profileOpt.get();
        List<DishHistory> dishHistory = dishHistoryRepository.findByProfile(profile);
        
        return dishHistory.stream()
                .map(dh -> {
                    Map<String, Object> historyMap = new HashMap<>();
                    historyMap.put("id", dh.getId());
                    historyMap.put("dishId", dh.getDish().getId());
                    historyMap.put("dishName", dh.getDish().getName());
                    historyMap.put("rating", dh.getUserRating());
                    historyMap.put("createdAt", dh.getCreatedAt().toString());
                    return historyMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * Process OpenAI recommendations and save dishes to database
     * @param menuText The menu text
     * @param openAIRecommendations The recommendations from OpenAI
     * @return List of processed dish recommendations
     */
    public List<Map<String, Object>> processOpenAIRecommendations(String menuText, String openAIRecommendations) {
        // This is a simplified implementation
        // In a real application, you would parse the JSON response from OpenAI
        
        // Process dishes from menu text
        List<Dish> dishes = dishService.processDishesFromMenuText(menuText);
        
        // For now, return a mock response
        return dishes.stream()
                .limit(3)
                .map(dish -> {
                    Map<String, Object> recommendationMap = new HashMap<>();
                    recommendationMap.put("dishId", dish.getId());
                    recommendationMap.put("dishName", dish.getName());
                    recommendationMap.put("explanation", "This dish matches your preferences.");
                    recommendationMap.put("matchScore", 85);
                    return recommendationMap;
                })
                .collect(Collectors.toList());
    }
}
