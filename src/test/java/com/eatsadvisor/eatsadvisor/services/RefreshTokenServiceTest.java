package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.RefreshToken;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import com.eatsadvisor.eatsadvisor.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private AppUser testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        
        // Set up test token
        testToken = new RefreshToken();
        testToken.setId(1);
        testToken.setUser(testUser);
        testToken.setToken("test-refresh-token");
        testToken.setExpiry(Instant.now().plus(7, ChronoUnit.DAYS));
        
        // Set up environment variables
        ReflectionTestUtils.setField(refreshTokenService, "googleClientId", "test-client-id");
        ReflectionTestUtils.setField(refreshTokenService, "googleClientSecret", "test-client-secret");
    }

    @Test
    void deleteRefreshToken_WithValidToken_ShouldDeleteToken() {
        // Arrange
        when(refreshTokenRepository.findByToken("test-refresh-token")).thenReturn(Optional.of(testToken));
        doNothing().when(refreshTokenRepository).delete(testToken);
        
        // Act
        refreshTokenService.deleteRefreshToken("test-refresh-token");
        
        // Assert
        verify(refreshTokenRepository, times(1)).findByToken("test-refresh-token");
        verify(refreshTokenRepository, times(1)).delete(testToken);
    }

    @Test
    void deleteRefreshToken_WithInvalidToken_ShouldDoNothing() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());
        
        // Act
        refreshTokenService.deleteRefreshToken("invalid-token");
        
        // Assert
        verify(refreshTokenRepository, times(1)).findByToken("invalid-token");
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_WithValidUserId_ShouldCreateToken() {
        // Arrange
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);
        
        // Act
        String result = refreshTokenService.createRefreshToken(1L);
        
        // Assert
        assertNotNull(result);
        verify(appUserRepository, times(1)).findById(1L);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_WithInvalidUserId_ShouldThrowException() {
        // Arrange
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> refreshTokenService.createRefreshToken(99L));
        verify(appUserRepository, times(1)).findById(99L);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_WithValidNonExpiredToken_ShouldReturnTrue() {
        // Arrange
        when(refreshTokenRepository.findByToken("test-refresh-token")).thenReturn(Optional.of(testToken));
        
        // Act
        boolean result = refreshTokenService.validateRefreshToken("test-refresh-token");
        
        // Assert
        assertTrue(result);
        verify(refreshTokenRepository, times(1)).findByToken("test-refresh-token");
    }

    @Test
    void validateRefreshToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiry(Instant.now().minus(1, ChronoUnit.DAYS));
        
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        
        // Act
        boolean result = refreshTokenService.validateRefreshToken("expired-token");
        
        // Assert
        assertFalse(result);
        verify(refreshTokenRepository, times(1)).findByToken("expired-token");
    }

    @Test
    void validateRefreshToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());
        
        // Act
        boolean result = refreshTokenService.validateRefreshToken("invalid-token");
        
        // Assert
        assertFalse(result);
        verify(refreshTokenRepository, times(1)).findByToken("invalid-token");
    }

    @Test
    void refreshAccessToken_WithValidToken_ShouldReturnNewAccessToken() {
        // Arrange
        when(refreshTokenRepository.findByToken("test-refresh-token")).thenReturn(Optional.of(testToken));
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", "new-access-token");
        
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, org.springframework.http.HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // Act
        String result = refreshTokenService.refreshAccessToken("test-refresh-token");
        
        // Assert
        assertEquals("new-access-token", result);
        verify(refreshTokenRepository, times(1)).findByToken("test-refresh-token");
        verify(restTemplate, times(1)).exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void refreshAccessToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> refreshTokenService.refreshAccessToken("invalid-token"));
        verify(refreshTokenRepository, times(1)).findByToken("invalid-token");
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void refreshAccessToken_WithExpiredToken_ShouldThrowException() {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiry(Instant.now().minus(1, ChronoUnit.DAYS));
        
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        doNothing().when(refreshTokenRepository).delete(expiredToken);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> refreshTokenService.refreshAccessToken("expired-token"));
        verify(refreshTokenRepository, times(1)).findByToken("expired-token");
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void deleteExpiredRefreshTokens_ShouldDeleteExpiredTokens() {
        // Arrange
        List<RefreshToken> expiredTokens = new ArrayList<>();
        RefreshToken expiredToken1 = new RefreshToken();
        RefreshToken expiredToken2 = new RefreshToken();
        expiredTokens.add(expiredToken1);
        expiredTokens.add(expiredToken2);
        
        when(refreshTokenRepository.findByExpiryBefore(any(Instant.class))).thenReturn(expiredTokens);
        doNothing().when(refreshTokenRepository).deleteAll(expiredTokens);
        
        // Act
        refreshTokenService.deleteExpiredRefreshTokens();
        
        // Assert
        verify(refreshTokenRepository, times(1)).findByExpiryBefore(any(Instant.class));
        verify(refreshTokenRepository, times(1)).deleteAll(expiredTokens);
    }
}
