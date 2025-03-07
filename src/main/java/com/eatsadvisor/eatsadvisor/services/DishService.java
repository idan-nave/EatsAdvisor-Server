package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Dish;
import com.eatsadvisor.eatsadvisor.models.DishHistory;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.repositories.DishHistoryRepository;
import com.eatsadvisor.eatsadvisor.repositories.DishRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DishService {
    private final DishRepository dishRepository;
    private final DishHistoryRepository dishHistoryRepository;
    private final ProfileRepository profileRepository;

    public DishService(
            DishRepository dishRepository,
            DishHistoryRepository dishHistoryRepository,
            ProfileRepository profileRepository) {
        this.dishRepository = dishRepository;
        this.dishHistoryRepository = dishHistoryRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Get all dishes
     * @return List of all dishes
     */
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    /**
     * Get a dish by ID
     * @param id The dish ID
     * @return The dish if found
     */
    public Optional<Dish> getDishById(Integer id) {
        return dishRepository.findById(id);
    }

    /**
     * Get a dish by name
     * @param name The dish name
     * @return The dish if found
     */
    public Optional<Dish> getDishByName(String name) {
        return dishRepository.findByName(name);
    }

    /**
     * Search dishes by name (case insensitive)
     * @param name The name to search for
     * @return List of matching dishes
     */
    public List<Dish> searchDishesByName(String name) {
        return dishRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Search dishes by description (case insensitive)
     * @param description The description to search for
     * @return List of matching dishes
     */
    public List<Dish> searchDishesByDescription(String description) {
        return dishRepository.findByDescriptionContainingIgnoreCase(description);
    }

    /**
     * Create a new dish
     * @param name The dish name
     * @param description The dish description (optional)
     * @return The created dish
     */
    @Transactional
    public Dish createDish(String name, String description) {
        // Check if dish already exists
        if (dishRepository.existsByName(name)) {
            throw new IllegalArgumentException("Dish with name '" + name + "' already exists");
        }
        
        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setCreatedAt(Instant.now());
        
        return dishRepository.save(dish);
    }

    /**
     * Update an existing dish
     * @param id The dish ID
     * @param name The new name (optional)
     * @param description The new description (optional)
     * @return The updated dish
     */
    @Transactional
    public Dish updateDish(Integer id, String name, String description) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found with ID: " + id));
        
        // Update name if provided
        if (name != null && !name.isEmpty()) {
            // Check if the new name already exists for a different dish
            Optional<Dish> existingDish = dishRepository.findByName(name);
            if (existingDish.isPresent() && !existingDish.get().getId().equals(id)) {
                throw new IllegalArgumentException("Dish with name '" + name + "' already exists");
            }
            dish.setName(name);
        }
        
        // Update description if provided
        if (description != null) {
            dish.setDescription(description);
        }
        
        return dishRepository.save(dish);
    }

    /**
     * Delete a dish
     * @param id The dish ID
     */
    @Transactional
    public void deleteDish(Integer id) {
        // Check if dish exists
        if (!dishRepository.existsById(id)) {
            throw new RuntimeException("Dish not found with ID: " + id);
        }
        
        dishRepository.deleteById(id);
    }

    /**
     * Get dishes rated by a specific profile
     * @param profileId The profile ID
     * @return List of dishes rated by the profile
     */
    public List<Dish> getDishesByProfileId(Integer profileId) {
        return dishRepository.findByProfileId(profileId);
    }

    /**
     * Get dishes rated by a specific profile with a minimum rating
     * @param profileId The profile ID
     * @param minRating The minimum rating
     * @return List of dishes rated by the profile with at least the specified rating
     */
    public List<Dish> getDishesByProfileIdAndMinRating(Integer profileId, Integer minRating) {
        return dishRepository.findByProfileIdAndMinRating(profileId, minRating);
    }

    /**
     * Get the average rating for a dish
     * @param dishId The dish ID
     * @return The average rating for the dish
     */
    public Double getAverageRatingForDish(Integer dishId) {
        return dishRepository.findAverageRatingByDishId(dishId);
    }

    /**
     * Get the most popular dishes
     * @param limit The maximum number of dishes to return
     * @return List of dishes with their average ratings
     */
    public List<Object[]> getMostPopularDishes(Integer limit) {
        return dishRepository.findMostPopularDishes(limit);
    }

    /**
     * Get dish history for a profile
     * @param profileId The profile ID
     * @return List of dish history entries
     */
    public List<DishHistory> getDishHistoryByProfileId(Integer profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        return dishHistoryRepository.findByProfile(profile);
    }

    /**
     * Get the most recent dish history entries for a profile
     * @param profileId The profile ID
     * @param limit The maximum number of entries to return
     * @return List of the most recent dish history entries
     */
    public List<DishHistory> getMostRecentDishHistoryByProfileId(Integer profileId, Integer limit) {
        return dishHistoryRepository.findMostRecentByProfileId(profileId, limit);
    }

    /**
     * Rate a dish
     * @param profileId The profile ID
     * @param dishId The dish ID
     * @param rating The rating (1-5)
     * @return The created or updated dish history entry
     */
    @Transactional
    public DishHistory rateDish(Integer profileId, Integer dishId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found with ID: " + dishId));
        
        // Check if already exists
        Optional<DishHistory> existingOpt = dishHistoryRepository.findByProfileAndDish(profile, dish);
        
        DishHistory dishHistory;
        if (existingOpt.isPresent()) {
            dishHistory = existingOpt.get();
            dishHistory.setUserRating(rating);
        } else {
            dishHistory = new DishHistory();
            dishHistory.setProfile(profile);
            dishHistory.setDish(dish);
            dishHistory.setUserRating(rating);
            dishHistory.setCreatedAt(Instant.now());
        }
        
        return dishHistoryRepository.save(dishHistory);
    }

    /**
     * Delete a dish history entry
     * @param profileId The profile ID
     * @param dishId The dish ID
     */
    @Transactional
    public void deleteDishHistory(Integer profileId, Integer dishId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found with ID: " + dishId));
        
        dishHistoryRepository.deleteByProfileAndDish(profile, dish);
    }

    /**
     * Get dish history for recommendation
     * @param profileId The profile ID
     * @return Map of dish names to ratings
     */
    public Map<String, Integer> getDishHistoryForRecommendation(Integer profileId) {
        List<DishHistory> dishHistory = getDishHistoryByProfileId(profileId);
        
        Map<String, Integer> dishMap = new HashMap<>();
        for (DishHistory dh : dishHistory) {
            dishMap.put(dh.getDish().getName(), dh.getUserRating());
        }
        
        return dishMap;
    }

    /**
     * Get or create a dish by name
     * @param name The dish name
     * @param description The dish description (optional)
     * @return The existing or created dish
     */
    @Transactional
    public Dish getOrCreateDish(String name, String description) {
        Optional<Dish> existingDish = dishRepository.findByName(name);
        if (existingDish.isPresent()) {
            return existingDish.get();
        }
        
        return createDish(name, description);
    }

    /**
     * Process dishes from menu text
     * This method extracts dish names from menu text and creates dishes if they don't exist
     * @param menuText The menu text
     * @return List of created or existing dishes
     */
    @Transactional
    public List<Dish> processDishesFromMenuText(String menuText) {
        // This is a simplified implementation
        // In a real application, you would use NLP or other techniques to extract dish names
        
        // Split by newlines and process each line
        String[] lines = menuText.split("\n");
        
        return java.util.Arrays.stream(lines)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    // Extract dish name (simplified)
                    String dishName = line.split("[\\-\\:]")[0].trim();
                    
                    // Extract description (simplified)
                    String description = line.length() > dishName.length() ? 
                            line.substring(dishName.length()).trim() : "";
                    
                    // Clean up description
                    description = description.replaceAll("^[\\-\\:\\s]+", "").trim();
                    
                    // Create or get dish
                    return getOrCreateDish(dishName, description);
                })
                .collect(Collectors.toList());
    }
}
