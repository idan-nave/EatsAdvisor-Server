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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DishServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private DishRepository dishRepository;
    
    @Mock
    private DishHistoryRepository dishHistoryRepository;
    
    @InjectMocks
    private DishService dishService;
    
    private Profile testProfile;
    private Dish testDish;
    private DishHistory testDishHistory;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test profile
        testProfile = new Profile();
        testProfile.setId(1);
        testProfile.setCreatedAt(Instant.now());
        
        // Set up test dish
        testDish = new Dish();
        testDish.setId(1);
        testDish.setName("Pizza");
        testDish.setCreatedAt(Instant.now());
        
        // Set up test dish history
        testDishHistory = new DishHistory();
        testDishHistory.setProfile(testProfile);
        testDishHistory.setDish(testDish);
        testDishHistory.setUserRating(4);
        testDishHistory.setCreatedAt(Instant.now());
        
        // Mock repository methods
        when(profileRepository.findById(1)).thenReturn(Optional.of(testProfile));
        when(dishRepository.findById(1)).thenReturn(Optional.of(testDish));
        when(dishHistoryRepository.save(any(DishHistory.class))).thenReturn(testDishHistory);
    }
    
    @Test
    void testGetAllDishes() {
        // Set up test data
        List<Dish> dishes = Arrays.asList(testDish, createDish(2, "Sushi"));
        when(dishRepository.findAll()).thenReturn(dishes);
        
        // Execute the method
        List<Dish> result = dishService.getAllDishes();
        
        // Verify interactions
        verify(dishRepository, times(1)).findAll();
        
        // Verify result
        assertEquals(2, result.size());
        assertEquals("Pizza", result.get(0).getName());
        assertEquals("Sushi", result.get(1).getName());
    }
    
    @Test
    void testGetDishById() {
        // Execute the method
        Optional<Dish> result = dishService.getDishById(1);
        
        // Verify interactions
        verify(dishRepository, times(1)).findById(1);
        
        // Verify result
        assertTrue(result.isPresent());
        assertEquals(testDish.getId(), result.get().getId());
        assertEquals(testDish.getName(), result.get().getName());
    }
    
    @Test
    void testGetDishById_NotFound() {
        // Set up test data
        when(dishRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method
        Optional<Dish> result = dishService.getDishById(999);
        
        // Verify interactions
        verify(dishRepository, times(1)).findById(999);
        
        // Verify result
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetDishByName() {
        // Set up test data
        when(dishRepository.findByName("Pizza")).thenReturn(Optional.of(testDish));
        
        // Execute the method
        Optional<Dish> result = dishService.getDishByName("Pizza");
        
        // Verify interactions
        verify(dishRepository, times(1)).findByName("Pizza");
        
        // Verify result
        assertTrue(result.isPresent());
        assertEquals(testDish.getId(), result.get().getId());
        assertEquals(testDish.getName(), result.get().getName());
    }
    
    @Test
    void testGetDishByName_NotFound() {
        // Set up test data
        when(dishRepository.findByName("NonExistentDish")).thenReturn(Optional.empty());
        
        // Execute the method
        Optional<Dish> result = dishService.getDishByName("NonExistentDish");
        
        // Verify interactions
        verify(dishRepository, times(1)).findByName("NonExistentDish");
        
        // Verify result
        assertFalse(result.isPresent());
    }
    
    @Test
    void testSearchDishesByName() {
        // Set up test data
        List<Dish> dishes = Arrays.asList(testDish);
        when(dishRepository.findByNameContainingIgnoreCase("pizza")).thenReturn(dishes);
        
        // Execute the method
        List<Dish> result = dishService.searchDishesByName("pizza");
        
        // Verify interactions
        verify(dishRepository, times(1)).findByNameContainingIgnoreCase("pizza");
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
    }
    
    @Test
    void testSearchDishesByDescription() {
        // Set up test data
        testDish.setDescription("Delicious pizza with cheese");
        List<Dish> dishes = Arrays.asList(testDish);
        when(dishRepository.findByDescriptionContainingIgnoreCase("cheese")).thenReturn(dishes);
        
        // Execute the method
        List<Dish> result = dishService.searchDishesByDescription("cheese");
        
        // Verify interactions
        verify(dishRepository, times(1)).findByDescriptionContainingIgnoreCase("cheese");
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
        assertEquals("Delicious pizza with cheese", result.get(0).getDescription());
    }
    
    @Test
    void testCreateDish() {
        // Set up test data
        when(dishRepository.existsByName("Pasta")).thenReturn(false);
        
        Dish newDish = new Dish();
        newDish.setId(3);
        newDish.setName("Pasta");
        newDish.setDescription("Italian pasta dish");
        newDish.setCreatedAt(Instant.now());
        
        when(dishRepository.save(any(Dish.class))).thenReturn(newDish);
        
        // Execute the method
        Dish result = dishService.createDish("Pasta", "Italian pasta dish");
        
        // Verify interactions
        verify(dishRepository, times(1)).existsByName("Pasta");
        verify(dishRepository, times(1)).save(any(Dish.class));
        
        // Verify result
        assertEquals("Pasta", result.getName());
        assertEquals("Italian pasta dish", result.getDescription());
    }
    
    @Test
    void testCreateDish_AlreadyExists() {
        // Set up test data
        when(dishRepository.existsByName("Pizza")).thenReturn(true);
        
        // Execute the method and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dishService.createDish("Pizza", "Another pizza description");
        });
        
        // Verify message
        assertEquals("Dish with name 'Pizza' already exists", exception.getMessage());
        
        // Verify interactions
        verify(dishRepository, times(1)).existsByName("Pizza");
        verify(dishRepository, never()).save(any(Dish.class));
    }
    
    @Test
    void testUpdateDish() {
        // Set up test data
        when(dishRepository.save(testDish)).thenReturn(testDish);
        
        // Execute the method
        Dish result = dishService.updateDish(1, "Updated Pizza", "Updated description");
        
        // Verify interactions
        verify(dishRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findByName("Updated Pizza");
        verify(dishRepository, times(1)).save(testDish);
        
        // Verify result
        assertEquals("Updated Pizza", result.getName());
        assertEquals("Updated description", result.getDescription());
    }
    
    @Test
    void testUpdateDish_NotFound() {
        // Set up test data
        when(dishRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.updateDish(999, "Updated Name", "Updated description");
        });
        
        // Verify message
        assertEquals("Dish not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(dishRepository, times(1)).findById(999);
        verify(dishRepository, never()).save(any(Dish.class));
    }
    
    @Test
    void testUpdateDish_NameAlreadyExists() {
        // Set up test data
        Dish existingDish = new Dish();
        existingDish.setId(2);
        existingDish.setName("Sushi");
        
        when(dishRepository.findByName("Sushi")).thenReturn(Optional.of(existingDish));
        
        // Execute the method and verify exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dishService.updateDish(1, "Sushi", "Updated description");
        });
        
        // Verify message
        assertEquals("Dish with name 'Sushi' already exists", exception.getMessage());
        
        // Verify interactions
        verify(dishRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findByName("Sushi");
        verify(dishRepository, never()).save(any(Dish.class));
    }
    
    @Test
    void testDeleteDish() {
        // Set up test data
        when(dishRepository.existsById(1)).thenReturn(true);
        
        // Execute the method
        dishService.deleteDish(1);
        
        // Verify interactions
        verify(dishRepository, times(1)).existsById(1);
        verify(dishRepository, times(1)).deleteById(1);
    }
    
    @Test
    void testDeleteDish_NotFound() {
        // Set up test data
        when(dishRepository.existsById(999)).thenReturn(false);
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.deleteDish(999);
        });
        
        // Verify message
        assertEquals("Dish not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(dishRepository, times(1)).existsById(999);
        verify(dishRepository, never()).deleteById(anyInt());
    }
    
    @Test
    void testGetDishesByProfileIdAndMinRating() {
        // Set up test data
        List<Dish> dishes = Arrays.asList(testDish);
        when(dishRepository.findByProfileIdAndMinRating(1, 4)).thenReturn(dishes);
        
        // Execute the method
        List<Dish> result = dishService.getDishesByProfileIdAndMinRating(1, 4);
        
        // Verify interactions
        verify(dishRepository, times(1)).findByProfileIdAndMinRating(1, 4);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
    }
    
    @Test
    void testGetAverageRatingForDish() {
        // Set up test data
        when(dishRepository.findAverageRatingByDishId(1)).thenReturn(4.5);
        
        // Execute the method
        Double result = dishService.getAverageRatingForDish(1);
        
        // Verify interactions
        verify(dishRepository, times(1)).findAverageRatingByDishId(1);
        
        // Verify result
        assertEquals(4.5, result);
    }
    
    @Test
    void testGetMostPopularDishes() {
        // Set up test data
        Object[] dish1Data = new Object[] {testDish, 4.5};
        Object[] dish2Data = new Object[] {createDish(2, "Sushi"), 4.0};
        List<Object[]> popularDishes = Arrays.asList(dish1Data, dish2Data);
        
        when(dishRepository.findMostPopularDishes(2)).thenReturn(popularDishes);
        
        // Execute the method
        List<Object[]> result = dishService.getMostPopularDishes(2);
        
        // Verify interactions
        verify(dishRepository, times(1)).findMostPopularDishes(2);
        
        // Verify result
        assertEquals(2, result.size());
        assertEquals(testDish, result.get(0)[0]);
        assertEquals(4.5, result.get(0)[1]);
    }
    
    @Test
    void testGetDishHistoryByProfileId() {
        // Set up test data
        List<DishHistory> dishHistory = Collections.singletonList(testDishHistory);
        when(dishHistoryRepository.findByProfile(testProfile)).thenReturn(dishHistory);
        
        // Execute the method
        List<DishHistory> result = dishService.getDishHistoryByProfileId(1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishHistoryRepository, times(1)).findByProfile(testProfile);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testDishHistory.getProfile(), result.get(0).getProfile());
        assertEquals(testDishHistory.getDish(), result.get(0).getDish());
        assertEquals(testDishHistory.getUserRating(), result.get(0).getUserRating());
    }
    
    @Test
    void testGetDishHistoryByProfileId_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.getDishHistoryByProfileId(999);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(dishHistoryRepository, never()).findByProfile(any());
    }
    
    @Test
    void testGetMostRecentDishHistoryByProfileId() {
        // Set up test data
        List<DishHistory> recentHistory = Collections.singletonList(testDishHistory);
        when(dishHistoryRepository.findMostRecentByProfileId(1, 5)).thenReturn(recentHistory);
        
        // Execute the method
        List<DishHistory> result = dishService.getMostRecentDishHistoryByProfileId(1, 5);
        
        // Verify interactions
        verify(dishHistoryRepository, times(1)).findMostRecentByProfileId(1, 5);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testDishHistory.getProfile(), result.get(0).getProfile());
        assertEquals(testDishHistory.getDish(), result.get(0).getDish());
    }
    
    @Test
    void testGetDishesByProfileId() {
        // Set up test data
        List<Dish> dishes = Collections.singletonList(testDish);
        when(dishRepository.findByProfileId(1)).thenReturn(dishes);
        
        // Execute the method
        List<Dish> result = dishService.getDishesByProfileId(1);
        
        // Verify interactions
        verify(dishRepository, times(1)).findByProfileId(1);
        
        // Verify result
        assertEquals(1, result.size());
        assertEquals(testDish.getId(), result.get(0).getId());
        assertEquals(testDish.getName(), result.get(0).getName());
    }
    
    @Test
    void testRateDish_New() {
        // Set up test data
        when(dishHistoryRepository.findByProfileAndDish(testProfile, testDish))
                .thenReturn(Optional.empty());
        
        // Execute the method
        DishHistory result = dishService.rateDish(1, 1, 4);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findById(1);
        verify(dishHistoryRepository, times(1)).findByProfileAndDish(testProfile, testDish);
        verify(dishHistoryRepository, times(1)).save(any(DishHistory.class));
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(testDish, result.getDish());
        assertEquals(4, result.getUserRating());
    }
    
    @Test
    void testRateDish_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.rateDish(999, 1, 4);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(dishRepository, never()).findById(anyInt());
        verify(dishHistoryRepository, never()).findByProfileAndDish(any(), any());
        verify(dishHistoryRepository, never()).save(any());
    }
    
    @Test
    void testRateDish_DishNotFound() {
        // Set up test data
        when(dishRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.rateDish(1, 999, 4);
        });
        
        // Verify message
        assertEquals("Dish not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findById(999);
        verify(dishHistoryRepository, never()).findByProfileAndDish(any(), any());
        verify(dishHistoryRepository, never()).save(any());
    }
    
    @Test
    void testRateDish_Update() {
        // Set up test data
        when(dishHistoryRepository.findByProfileAndDish(testProfile, testDish))
                .thenReturn(Optional.of(testDishHistory));
        
        // Execute the method
        DishHistory result = dishService.rateDish(1, 1, 5);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findById(1);
        verify(dishHistoryRepository, times(1)).findByProfileAndDish(testProfile, testDish);
        verify(dishHistoryRepository, times(1)).save(testDishHistory);
        
        // Verify result
        assertEquals(testProfile, result.getProfile());
        assertEquals(testDish, result.getDish());
        assertEquals(5, result.getUserRating());
    }
    
    @Test
    void testRateDish_InvalidRating() {
        // Test with rating < 1
        assertThrows(IllegalArgumentException.class, () -> {
            dishService.rateDish(1, 1, 0);
        });
        
        // Test with rating > 5
        assertThrows(IllegalArgumentException.class, () -> {
            dishService.rateDish(1, 1, 6);
        });
    }
    
    @Test
    void testDeleteDishHistory() {
        // Execute the method
        dishService.deleteDishHistory(1, 1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findById(1);
        verify(dishHistoryRepository, times(1)).deleteByProfileAndDish(testProfile, testDish);
    }
    
    @Test
    void testDeleteDishHistory_ProfileNotFound() {
        // Set up test data
        when(profileRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.deleteDishHistory(999, 1);
        });
        
        // Verify message
        assertEquals("Profile not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(999);
        verify(dishRepository, never()).findById(anyInt());
        verify(dishHistoryRepository, never()).deleteByProfileAndDish(any(), any());
    }
    
    @Test
    void testDeleteDishHistory_DishNotFound() {
        // Set up test data
        when(dishRepository.findById(999)).thenReturn(Optional.empty());
        
        // Execute the method and verify exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dishService.deleteDishHistory(1, 999);
        });
        
        // Verify message
        assertEquals("Dish not found with ID: 999", exception.getMessage());
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishRepository, times(1)).findById(999);
        verify(dishHistoryRepository, never()).deleteByProfileAndDish(any(), any());
    }
    
    @Test
    void testGetDishHistoryForRecommendation() {
        // Set up test data
        List<DishHistory> dishHistory = Arrays.asList(
            testDishHistory,
            createDishHistory(2, "Sushi", 5)
        );
        when(dishHistoryRepository.findByProfile(testProfile)).thenReturn(dishHistory);
        
        // Execute the method
        Map<String, Integer> result = dishService.getDishHistoryForRecommendation(1);
        
        // Verify interactions
        verify(profileRepository, times(1)).findById(1);
        verify(dishHistoryRepository, times(1)).findByProfile(testProfile);
        
        // Verify result
        assertEquals(2, result.size());
        assertEquals(4, result.get("Pizza"));
        assertEquals(5, result.get("Sushi"));
    }
    
    @Test
    void testGetOrCreateDish_Existing() {
        // Set up test data
        when(dishRepository.findByName("Pizza")).thenReturn(Optional.of(testDish));
        
        // Execute the method
        Dish result = dishService.getOrCreateDish("Pizza", "Description");
        
        // Verify interactions
        verify(dishRepository, times(1)).findByName("Pizza");
        verify(dishRepository, never()).existsByName(anyString());
        verify(dishRepository, never()).save(any(Dish.class));
        
        // Verify result
        assertEquals(testDish, result);
    }
    
    @Test
    void testGetOrCreateDish_New() {
        // Set up test data
        when(dishRepository.findByName("Pasta")).thenReturn(Optional.empty());
        when(dishRepository.existsByName("Pasta")).thenReturn(false);
        
        Dish newDish = new Dish();
        newDish.setId(3);
        newDish.setName("Pasta");
        newDish.setDescription("Italian pasta dish");
        newDish.setCreatedAt(Instant.now());
        
        when(dishRepository.save(any(Dish.class))).thenReturn(newDish);
        
        // Execute the method
        Dish result = dishService.getOrCreateDish("Pasta", "Italian pasta dish");
        
        // Verify interactions
        verify(dishRepository, times(1)).findByName("Pasta");
        verify(dishRepository, times(1)).existsByName("Pasta");
        verify(dishRepository, times(1)).save(any(Dish.class));
        
        // Verify result
        assertEquals("Pasta", result.getName());
        assertEquals("Italian pasta dish", result.getDescription());
    }
    
    @Test
    void testProcessDishesFromMenuText() {
        // Set up test data
        String menuText = "Pizza - Delicious pizza with cheese\nSushi - Fresh sushi";
        
        when(dishRepository.findByName("Pizza")).thenReturn(Optional.of(testDish));
        when(dishRepository.findByName("Sushi")).thenReturn(Optional.empty());
        
        Dish sushiDish = new Dish();
        sushiDish.setId(2);
        sushiDish.setName("Sushi");
        sushiDish.setDescription("Fresh sushi");
        sushiDish.setCreatedAt(Instant.now());
        when(dishRepository.save(any(Dish.class))).thenReturn(sushiDish);
        
        // Execute the method
        List<Dish> result = dishService.processDishesFromMenuText(menuText);
        
        // Verify interactions
        verify(dishRepository, times(2)).findByName(anyString());
        verify(dishRepository, times(1)).save(any(Dish.class));
        
        // Verify result
        assertEquals(2, result.size());
        assertEquals("Pizza", result.get(0).getName());
        assertEquals("Sushi", result.get(1).getName());
    }
    
    @Test
    void testProcessDishesFromMenuText_InvalidFormat() {
        // Set up test data with invalid format
        String menuText = "Invalid menu text without proper format";
        
        // Execute the method
        List<Dish> result = dishService.processDishesFromMenuText(menuText);
        
        // Verify result - should still process it as a dish name
        assertEquals(1, result.size());
        assertEquals("Invalid menu text without proper format", result.get(0).getName());
    }
    
    // Helper method to create test dish
    private Dish createDish(Integer dishId, String dishName) {
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName(dishName);
        dish.setCreatedAt(Instant.now());
        return dish;
    }
    
    // Helper method to create test dish history
    private DishHistory createDishHistory(Integer dishId, String dishName, Integer rating) {
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName(dishName);
        dish.setCreatedAt(Instant.now());
        
        DishHistory dishHistory = new DishHistory();
        dishHistory.setProfile(testProfile);
        dishHistory.setDish(dish);
        dishHistory.setUserRating(rating);
        dishHistory.setCreatedAt(Instant.now());
        
        return dishHistory;
    }
}
