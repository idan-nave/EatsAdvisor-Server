package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setOauthProvider("google");
        testUser.setOauthProviderId("123456789");
    }

    @Test
    void findAppUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<AppUser> result = appUserService.findAppUserByEmail("test@example.com");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void findAppUserByEmail_WithInvalidEmail_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());
        
        // Act
        Optional<AppUser> result = appUserService.findAppUserByEmail("invalid@example.com");
        
        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("invalid@example.com");
    }

    @Test
    void updateUserPreferences_WithValidUser_ShouldUpdateUser() {
        // Arrange
        AppUser updatedUser = new AppUser();
        updatedUser.setId(1);
        // Note: The actual preference update logic is commented out in the service
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);
        
        // Act
        AppUser result = appUserService.updateUserPreferences(updatedUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void updateUserPreferences_WithInvalidUser_ShouldThrowException() {
        // Arrange
        AppUser invalidUser = new AppUser();
        invalidUser.setId(99);
        
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> appUserService.updateUserPreferences(invalidUser));
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(AppUser.class));
    }
}
