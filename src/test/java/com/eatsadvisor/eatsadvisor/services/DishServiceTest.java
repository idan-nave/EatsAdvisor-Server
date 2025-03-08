package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Dish;
import com.eatsadvisor.eatsadvisor.repositories.DishHistoryRepository;
import com.eatsadvisor.eatsadvisor.repositories.DishRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private DishHistoryRepository dishHistoryRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private DishService dishService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetOrCreateDish_ExistingDish() {
        // Arrange
        String dishName = "Test Dish";
        String description = "Test Description";
        
        Dish existingDish = new Dish();
        existingDish.setId(1);
        existingDish.setName(dishName);
        existingDish.setDescription(description);
        
        when(dishRepository.findByName(dishName)).thenReturn(Optional.of(existingDish));
        
        // Act
        Dish result = dishService.getOrCreateDish(dishName, description);
        
        // Assert
        assertEquals(existingDish, result);
        verify(dishRepository).findByName(dishName);
        verify(dishRepository, never()).save(any(Dish.class));
    }

    @Test
    public void testGetOrCreateDish_NewDish() {
        // Arrange
        String dishName = "New Dish";
        String description = "New Description";
        
        Dish newDish = new Dish();
        newDish.setId(1);
        newDish.setName(dishName);
        newDish.setDescription(description);
        newDish.setCreatedAt(Instant.now());
        
        when(dishRepository.findByName(dishName)).thenReturn(Optional.empty());
        when(dishRepository.save(any(Dish.class))).thenReturn(newDish);
        
        // Act
        Dish result = dishService.getOrCreateDish(dishName, description);
        
        // Assert
        assertEquals(newDish, result);
        verify(dishRepository).findByName(dishName);
        verify(dishRepository).save(any(Dish.class));
    }

    @Test
    public void testProcessDishesFromMenuText_ValidInput() {
        // Arrange
        String menuText = "Dish 1 - Description 1\nDish 2 - Description 2";
        
        Dish dish1 = new Dish();
        dish1.setId(1);
        dish1.setName("Dish 1");
        dish1.setDescription("Description 1");
        
        Dish dish2 = new Dish();
        dish2.setId(2);
        dish2.setName("Dish 2");
        dish2.setDescription("Description 2");
        
        when(dishRepository.findByName("Dish 1")).thenReturn(Optional.empty());
        when(dishRepository.findByName("Dish 2")).thenReturn(Optional.empty());
        when(dishRepository.save(any(Dish.class)))
            .thenAnswer(invocation -> {
                Dish dish = invocation.getArgument(0);
                if (dish.getName().equals("Dish 1")) {
                    return dish1;
                } else {
                    return dish2;
                }
            });
        
        // Act
        List<Dish> result = dishService.processDishesFromMenuText(menuText);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Dish 1", result.get(0).getName());
        assertEquals("Dish 2", result.get(1).getName());
        verify(dishRepository, times(2)).save(any(Dish.class));
    }

    @Test
    public void testProcessDishesFromMenuText_EmptyLines() {
        // Arrange
        String menuText = "\n\nDish 1 - Description 1\n\nDish 2 - Description 2\n\n";
        
        Dish dish1 = new Dish();
        dish1.setId(1);
        dish1.setName("Dish 1");
        dish1.setDescription("Description 1");
        
        Dish dish2 = new Dish();
        dish2.setId(2);
        dish2.setName("Dish 2");
        dish2.setDescription("Description 2");
        
        when(dishRepository.findByName("Dish 1")).thenReturn(Optional.empty());
        when(dishRepository.findByName("Dish 2")).thenReturn(Optional.empty());
        when(dishRepository.save(any(Dish.class)))
            .thenAnswer(invocation -> {
                Dish dish = invocation.getArgument(0);
                if (dish.getName().equals("Dish 1")) {
                    return dish1;
                } else {
                    return dish2;
                }
            });
        
        // Act
        List<Dish> result = dishService.processDishesFromMenuText(menuText);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Dish 1", result.get(0).getName());
        assertEquals("Dish 2", result.get(1).getName());
    }

    @Test
    public void testProcessDishesFromMenuText_InvalidLines() {
        // Arrange
        String menuText = "Dish 1 - Description 1\n-\n:\nDish 2 - Description 2";
        
        Dish dish1 = new Dish();
        dish1.setId(1);
        dish1.setName("Dish 1");
        dish1.setDescription("Description 1");
        
        Dish dish2 = new Dish();
        dish2.setId(2);
        dish2.setName("Dish 2");
        dish2.setDescription("Description 2");
        
        when(dishRepository.findByName("Dish 1")).thenReturn(Optional.empty());
        when(dishRepository.findByName("Dish 2")).thenReturn(Optional.empty());
        when(dishRepository.save(any(Dish.class)))
            .thenAnswer(invocation -> {
                Dish dish = invocation.getArgument(0);
                if (dish.getName().equals("Dish 1")) {
                    return dish1;
                } else {
                    return dish2;
                }
            });
        
        // Act
        List<Dish> result = dishService.processDishesFromMenuText(menuText);
        
        // Assert
        // Should only process valid lines and skip invalid ones
        assertEquals(2, result.size());
        assertEquals("Dish 1", result.get(0).getName());
        assertEquals("Dish 2", result.get(1).getName());
    }
}
