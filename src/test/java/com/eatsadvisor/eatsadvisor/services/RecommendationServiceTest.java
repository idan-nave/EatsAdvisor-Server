package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.*;
import com.eatsadvisor.eatsadvisor.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

    @Mock
    private ProfileService profileService;
    
    @Mock
    private ProfileAllergyService profileAllergyService;
    
    @Mock
    private ProfileFlavorPreferenceService profileFlavorPreferenceService;
    
    @Mock
    private ProfileConstraintService profileConstraintService;
    
    @Mock
    private SpecialPreferenceService specialPreferenceService;
    
    @Mock
    private DishService dishService;
    
    @InjectMocks
    private RecommendationService recommendationService;
    
    private AppUser testUser;
    private Profile testProfile;
    private String testEmail = "test@example.com";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test user
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail(testEmail);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setUser(testUser);
        testProfile.setCreatedAt(Instant.now());
        
        // Mock profileService methods
        when(profileService.getProfileByEmail(testEmail)).thenReturn(Optional.of(testProfile));
    }
    
    @Test
    void testGetUserPreferencesForRecommendation() {
        // Set up test data
        List<Allergy> allergies = Arrays.asList(
            createAllergy(1, "Peanuts"),
            createAllergy(2, "Shellfish")
        );
        
        List<ProfileFlavorPreference> flavorPreferences = Arrays.asList(
            createFlavorPreference(1, "Sweet", 8),
            createFlavorPreference(2, "Spicy", 6)
        );
        
        List<ConstraintType> constraints = Arrays.asList(
            createConstraintType(1, "Vegetarian"),
            createConstraintType(2, "Gluten-Free")
        );
        
        List<SpecialPreference> specialPreferences = Collections.singletonList(
            createSpecialPreference(1, "I prefer organic ingredients")
        );
        
        Map<String, Integer> dishHistory = new HashMap<>();
        dishHistory.put("Pizza", 4);
        dishHistory.put("Sushi", 5);
        
        // Mock service methods
        when(profileAllergyService.getAllergiesByProfileId(testProfile.getId())).thenReturn(allergies);
        when(profileFlavorPreferenceService.getFlavorPreferencesByProfileId(testProfile.getId())).thenReturn(flavorPreferences);
        when(profileConstraintService.getConstraintsByProfileId(testProfile.getId())).thenReturn(constraints);
        when(specialPreferenceService.getSpecialPreferencesByProfileId(testProfile.getId())).thenReturn(specialPreferences);
        when(dishService.getDishHistoryForRecommendation(testProfile.getId())).thenReturn(dishHistory);
        
        // Execute the method
        Map<String, Object> result = recommendationService.getUserPreferencesForRecommendation(testEmail);
        
        // Verify interactions
        verify(profileService, times(1)).getProfileByEmail(testEmail);
        verify(profileAllergyService, times(1)).getAllergiesByProfileId(testProfile.getId());
        verify(profileFlavorPreferenceService, times(1)).getFlavorPreferencesByProfileId(testProfile.getId());
        verify(profileConstraintService, times(1)).getConstraintsByProfileId(testProfile.getId());
        verify(specialPreferenceService, times(1)).getSpecialPreferencesByProfileId(testProfile.getId());
        verify(dishService, times(1)).getDishHistoryForRecommendation(testProfile.getId());
        
        // Verify result
        assertNotNull(result);
        assertEquals(5, result.size());
        
        // Verify allergies
        List<String> resultAllergies = (List<String>) result.get("allergies");
        assertEquals(2, resultAllergies.size());
        assertTrue(resultAllergies.contains("Peanuts"));
        assertTrue(resultAllergies.contains("Shellfish"));
        
        // Verify flavor preferences
        Map<String, Integer> resultFlavorPrefs = (Map<String, Integer>) result.get("flavorPreferences");
        assertEquals(2, resultFlavorPrefs.size());
        assertEquals(8, resultFlavorPrefs.get("Sweet"));
        assertEquals(6, resultFlavorPrefs.get("Spicy"));
        
        // Verify constraints
        List<String> resultConstraints = (List<String>) result.get("dietaryConstraints");
        assertEquals(2, resultConstraints.size());
        assertTrue(resultConstraints.contains("Vegetarian"));
        assertTrue(resultConstraints.contains("Gluten-Free"));
        
        // Verify special preferences
        List<String> resultSpecialPrefs = (List<String>) result.get("specialPreferences");
        assertEquals(1, resultSpecialPrefs.size());
        assertEquals("I prefer organic ingredients", resultSpecialPrefs.get(0));
        
        // Verify dish history
        Map<String, Integer> resultDishHistory = (Map<String, Integer>) result.get("dishHistory");
        assertEquals(2, resultDishHistory.size());
        assertEquals(4, resultDishHistory.get("Pizza"));
        assertEquals(5, resultDishHistory.get("Sushi"));
    }
    
    @Test
    void testGetUserPreferencesForRecommendation_ProfileNotFound() {
        // Set up test data
        when(profileService.getProfileByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Execute the method
        Map<String, Object> result = recommendationService.getUserPreferencesForRecommendation("nonexistent@example.com");
        
        // Verify interactions
        verify(profileService, times(1)).getProfileByEmail("nonexistent@example.com");
        verify(profileAllergyService, never()).getAllergiesByProfileId(anyInt());
        verify(profileFlavorPreferenceService, never()).getFlavorPreferencesByProfileId(anyInt());
        verify(profileConstraintService, never()).getConstraintsByProfileId(anyInt());
        verify(specialPreferenceService, never()).getSpecialPreferencesByProfileId(anyInt());
        verify(dishService, never()).getDishHistoryForRecommendation(anyInt());
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testSetUserPreferences() {
        // Set up test data
        Map<String, Object> preferences = new HashMap<>();
        
        // Allergies
        List<String> allergies = Arrays.asList("Peanuts", "Shellfish");
        preferences.put("allergies", allergies);
        
        // Dietary constraints
        List<String> constraints = Arrays.asList("Vegetarian", "Gluten-Free");
        preferences.put("dietaryConstraints", constraints);
        
        // Flavor preferences
        Map<String, Integer> flavorPrefs = new HashMap<>();
        flavorPrefs.put("Sweet", 8);
        flavorPrefs.put("Spicy", 6);
        preferences.put("flavorPreferences", flavorPrefs);
        
        // Special preferences
        List<String> specialPrefs = Collections.singletonList("I prefer organic ingredients");
        preferences.put("specialPreferences", specialPrefs);
        
        // Execute the method
        recommendationService.setUserPreferences(testEmail, preferences);
        
        // Verify interactions
        verify(profileService, times(1)).getProfileByEmail(testEmail);
        verify(profileAllergyService, times(1)).processAllergies(testProfile, allergies);
        verify(profileConstraintService, times(1)).processConstraints(testProfile, constraints);
        verify(profileFlavorPreferenceService, times(1)).processFlavorPreferences(testProfile, flavorPrefs);
        verify(specialPreferenceService, times(1)).processSpecialPreferences(testProfile, specialPrefs);
    }
    
    @Test
    void testSetUserPreferences_ProfileNotFound() {
        // Set up test data
        when(profileService.getProfileByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("allergies", Arrays.asList("Peanuts", "Shellfish"));
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recommendationService.setUserPreferences("nonexistent@example.com", preferences);
        });
        
        // Verify message
        assertEquals("Profile not found for email: nonexistent@example.com", exception.getMessage());
        
        // Verify interactions
        verify(profileService, times(1)).getProfileByEmail("nonexistent@example.com");
        verify(profileAllergyService, never()).processAllergies(any(), any());
        verify(profileConstraintService, never()).processConstraints(any(), any());
        verify(profileFlavorPreferenceService, never()).processFlavorPreferences(any(), any());
        verify(specialPreferenceService, never()).processSpecialPreferences(any(), any());
    }
    
    @Test
    void testSetUserPreferences_PartialData() {
        // Set up test data with only some preference types
        Map<String, Object> preferences = new HashMap<>();
        
        // Only include allergies and flavor preferences
        List<String> allergies = Arrays.asList("Peanuts", "Shellfish");
        preferences.put("allergies", allergies);
        
        Map<String, Integer> flavorPrefs = new HashMap<>();
        flavorPrefs.put("Sweet", 8);
        flavorPrefs.put("Spicy", 6);
        preferences.put("flavorPreferences", flavorPrefs);
        
        // Execute the method
        recommendationService.setUserPreferences(testEmail, preferences);
        
        // Verify interactions
        verify(profileService, times(1)).getProfileByEmail(testEmail);
        verify(profileAllergyService, times(1)).processAllergies(testProfile, allergies);
        verify(profileFlavorPreferenceService, times(1)).processFlavorPreferences(testProfile, flavorPrefs);
        
        // These should not be called since they weren't included in the preferences
        verify(profileConstraintService, never()).processConstraints(any(), any());
        verify(specialPreferenceService, never()).processSpecialPreferences(any(), any());
    }
    
    @Test
    void testProcessDishes() {
        // Set up test data
        List<String> dishes = Arrays.asList("Pizza", "Sushi");
        
        Dish pizzaDish = new Dish();
        pizzaDish.setId(1);
        pizzaDish.setName("Pizza");
        pizzaDish.setCreatedAt(Instant.now());
        
        Dish sushiDish = new Dish();
        sushiDish.setId(2);
        sushiDish.setName("Sushi");
        sushiDish.setCreatedAt(Instant.now());
        
        DishHistory pizzaHistory = new DishHistory();
        pizzaHistory.setProfile(testProfile);
        pizzaHistory.setDish(pizzaDish);
        pizzaHistory.setCreatedAt(Instant.now());
        
        DishHistory sushiHistory = new DishHistory();
        sushiHistory.setProfile(testProfile);
        sushiHistory.setDish(sushiDish);
        sushiHistory.setCreatedAt(Instant.now());
        
        List<DishHistory> existingHistory = Arrays.asList(pizzaHistory, sushiHistory);
        
        when(dishService.getDishHistoryByProfileId(testProfile.getId())).thenReturn(existingHistory);
        when(dishService.getOrCreateDish("Pizza", null)).thenReturn(pizzaDish);
        when(dishService.getOrCreateDish("Sushi", null)).thenReturn(sushiDish);
        
        // Execute the method
        recommendationService.processDishes(testProfile, dishes);
        
        // Verify interactions
        verify(dishService, times(1)).getDishHistoryByProfileId(testProfile.getId());
        verify(dishService, times(1)).deleteDishHistory(eq(testProfile.getId()), eq(1));
        verify(dishService, times(1)).deleteDishHistory(eq(testProfile.getId()), eq(2));
        verify(dishService, times(2)).getOrCreateDish(anyString(), isNull());
        verify(dishService, times(2)).rateDish(eq(testProfile.getId()), anyInt(), eq(3));
    }
    
    @Test
    void testProcessDishes_EmptyOrNullDishes() {
        // Set up test data with empty and null dish names
        List<String> dishes = Arrays.asList("", null, "Pizza");
        
        Dish pizzaDish = new Dish();
        pizzaDish.setId(1);
        pizzaDish.setName("Pizza");
        pizzaDish.setCreatedAt(Instant.now());
        
        List<DishHistory> existingHistory = Collections.emptyList();
        
        when(dishService.getDishHistoryByProfileId(testProfile.getId())).thenReturn(existingHistory);
        when(dishService.getOrCreateDish("Pizza", null)).thenReturn(pizzaDish);
        
        // Execute the method
        recommendationService.processDishes(testProfile, dishes);
        
        // Verify interactions
        verify(dishService, times(1)).getDishHistoryByProfileId(testProfile.getId());
        verify(dishService, times(1)).getOrCreateDish(anyString(), isNull()); // Only for "Pizza"
        verify(dishService, times(1)).rateDish(eq(testProfile.getId()), anyInt(), eq(3)); // Only for "Pizza"
    }
    
    // Helper methods to create test objects
    
    private Allergy createAllergy(Integer id, String name) {
        Allergy allergy = new Allergy();
        allergy.setId(id);
        allergy.setName(name);
        allergy.setCreatedAt(Instant.now());
        return allergy;
    }
    
    private ProfileFlavorPreference createFlavorPreference(Integer id, String flavorName, Integer preferenceLevel) {
        ProfileFlavorPreference preference = new ProfileFlavorPreference();
        preference.setProfile(testProfile);
        
        Flavor flavor = new Flavor();
        flavor.setId(id);
        flavor.setName(flavorName);
        flavor.setCreatedAt(Instant.now());
        
        preference.setFlavor(flavor);
        preference.setPreferenceLevel(preferenceLevel);
        preference.setCreatedAt(Instant.now());
        
        return preference;
    }
    
    private ConstraintType createConstraintType(Integer id, String name) {
        ConstraintType constraintType = new ConstraintType();
        constraintType.setId(id);
        constraintType.setName(name);
        constraintType.setCreatedAt(Instant.now());
        return constraintType;
    }
    
    private SpecialPreference createSpecialPreference(Integer id, String description) {
        SpecialPreference specialPreference = new SpecialPreference();
        specialPreference.setId(id);
        specialPreference.setProfile(testProfile);
        specialPreference.setDescription(description);
        specialPreference.setCreatedAt(Instant.now());
        return specialPreference;
    }
}
