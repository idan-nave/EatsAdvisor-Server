package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Dish;
import com.eatsadvisor.eatsadvisor.models.DishHistory;
import com.eatsadvisor.eatsadvisor.models.Profile;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RecommendationService {
    private final ProfileService profileService;
    private final ProfileAllergyService profileAllergyService;
    private final ProfileFlavorPreferenceService profileFlavorPreferenceService;
    private final ProfileConstraintService profileConstraintService;
    private final SpecialPreferenceService specialPreferenceService;
    private final DishService dishService;

    public RecommendationService(
            @Lazy ProfileService profileService,
            ProfileAllergyService profileAllergyService,
            ProfileFlavorPreferenceService profileFlavorPreferenceService,
            ProfileConstraintService profileConstraintService,
            SpecialPreferenceService specialPreferenceService,
            DishService dishService) {
        this.profileService = profileService;
        this.profileAllergyService = profileAllergyService;
        this.profileFlavorPreferenceService = profileFlavorPreferenceService;
        this.profileConstraintService = profileConstraintService;
        this.specialPreferenceService = specialPreferenceService;
        this.dishService = dishService;
    }

    /**
     * Get user preferences for recommendation
     * This method collects all user preferences and formats them for the OpenAI
     * recommendation API
     * 
     * @param email The user's email
     * @return Map of user preferences
     */
    public Map<String, Object> getUserPreferencesForRecommendation(String email) {
        Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
        if (profileOpt.isEmpty()) {
            return new HashMap<>();
        }

        Profile profile = profileOpt.get();

        // Get allergies
        List<String> allergies = profileAllergyService.getAllergiesByProfileId(profile.getId())
                .stream()
                .map(allergy -> allergy.getName())
                .toList();

        // Get flavor preferences
        Map<String, Integer> flavorPreferences = new LinkedHashMap<>();
        profileFlavorPreferenceService.getFlavorPreferencesByProfileId(profile.getId())
                .forEach(pfp -> flavorPreferences.put(pfp.getFlavor().getName(), pfp.getPreferenceLevel()));

        // Get dietary constraints
        List<String> dietaryConstraints = profileConstraintService.getConstraintsByProfileId(profile.getId())
                .stream()
                .map(constraintType -> constraintType.getName())
                .toList();

        // Get special preferences
        List<String> specialPreferences = specialPreferenceService.getSpecialPreferencesByProfileId(profile.getId())
                .stream()
                .map(specialPreference -> specialPreference.getDescription())
                .toList();

        // Get dish history
        // List<DishHistory> dishHistory =
        // dishService.getDishHistoryForRecommendation(profile.getId());

        // Create the response map in the desired format
        Map<String, Object> response = new HashMap<>();
        response.put("allergies", allergies);
        response.put("dietaryConstraints", dietaryConstraints);
        response.put("flavorPreferences", flavorPreferences);
        response.put("specificDishes", List.of()); // Empty list for specificDishes
        response.put("specialPreferences", specialPreferences);
        response.put("dishHistory", new HashMap<>()); // Empty map for dishHistory

        return response;
    }

    /**
     * Set all user preferences
     * 
     * @param email       The user's email
     * @param preferences Map containing all user preferences
     */
    public void setUserPreferences(String email, Map<String, Object> preferences) {
        // Get profile by email
        Optional<Profile> profileOpt = profileService.getProfileByEmail(email);
        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Profile not found for email: " + email);
        }

        Profile profile = profileOpt.get();

        // Process allergies
        if (preferences.containsKey("allergies")) {
            List<String> allergies = (List<String>) preferences.get("allergies");
            profileAllergyService.processAllergies(profile, allergies);
        }

        // Process dietary constraints
        if (preferences.containsKey("dietaryConstraints")) {
            List<String> constraints = (List<String>) preferences.get("dietaryConstraints");
            profileConstraintService.processConstraints(profile, constraints);
        }

        // Process flavor preferences
        if (preferences.containsKey("flavorPreferences")) {
            Map<String, Integer> flavorPrefs = (Map<String, Integer>) preferences.get("flavorPreferences");
            profileFlavorPreferenceService.processFlavorPreferences(profile, flavorPrefs);
        }

        // Process special preferences
        if (preferences.containsKey("specialPreferences")) {
            List<String> specialPrefs = (List<String>) preferences.get("specialPreferences");
            specialPreferenceService.processSpecialPreferences(profile, specialPrefs);
        }

        // Process specific dishes
        if (preferences.containsKey("specificDishes") && preferences.get("specificDishes") != null) {
            List<String> dishes = (List<String>) preferences.get("specificDishes");
            if (dishes != null && !dishes.isEmpty()) {
                processDishes(profile, dishes);
            }
        }
    }

    /**
     * Process dishes for a profile
     * 
     * @param profile The profile
     * @param dishes  List of dish names
     */
    public void processDishes(Profile profile, List<String> dishes) {
        // Get existing dish history
        List<DishHistory> existingHistory = dishService.getDishHistoryByProfileId(profile.getId());

        // Delete existing dish history
        for (DishHistory history : existingHistory) {
            dishService.deleteDishHistory(profile.getId(), history.getDish().getId());
        }

        // Add new dishes
        for (String dishName : dishes) {
            if (dishName == null || dishName.trim().isEmpty()) {
                continue;
            }

            // Get or create dish
            Dish dish = dishService.getOrCreateDish(dishName, null);

            // Create dish history entry (with default rating of 3)
            dishService.rateDish(profile.getId(), dish.getId(), 3);
        }
    }
}