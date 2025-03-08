package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.RefreshToken;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import com.eatsadvisor.eatsadvisor.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;

    @Value("${GOOGLE_OAUTH_CLIENT_ID}")
    private String googleClientId;

    @Value("${GOOGLE_OAUTH_CLIENT_SECRET}")
    private String googleClientSecret;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AppUserRepository appUserRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Delete a refresh token from the database
     */
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Create a new refresh token for a user
     */
    public String createRefreshToken(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiry(Instant.now().plus(7, ChronoUnit.DAYS)); // Set expiry to 1 week

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    /**
     * Validate if a refresh token exists and is not expired
     */
    public boolean validateRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> token.getExpiry().isAfter(Instant.now()))
                .isPresent();
    }

    /**
     * Refresh access token using Google OAuth2 API
     */
    public String refreshAccessToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.getExpiry().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        // Request a new access token from Google
        Map<String, String> body = new HashMap<>();
        body.put("client_id", googleClientId);
        body.put("client_secret", googleClientSecret);
        body.put("refresh_token", storedToken.getToken());
        body.put("grant_type", "refresh_token");

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                org.springframework.http.HttpMethod.POST,
                new HttpEntity<>(body),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to refresh access token");
        }

        return responseBody.get("access_token").toString();
    }

    /**
     * Generate a new JWT using the refreshed access token
     */
    public String generateNewJwt(String refreshToken) {
        String newAccessToken = refreshAccessToken(refreshToken);

        // Request ID token (JWT) from Google
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(newAccessToken);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/tokeninfo",
                org.springframework.http.HttpMethod.POST,
                new HttpEntity<>(headers),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("id_token")) {
            throw new RuntimeException("Failed to retrieve new JWT");
        }

        return responseBody.get("id_token").toString();
    }

    /**
     * Delete expired refresh tokens
     */
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void deleteExpiredRefreshTokens() {
        Instant now = Instant.now();
        List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiryBefore(now);
        refreshTokenRepository.deleteAll(expiredTokens);
        System.out.println("✅ RefreshTokenService: Deleted " + expiredTokens.size() + " expired refresh tokens");
    }
}
