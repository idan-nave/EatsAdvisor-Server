package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.RefreshToken;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import com.eatsadvisor.eatsadvisor.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    public String createRefreshToken(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("User not found")
        );

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiry(Instant.now().plus(7, ChronoUnit.DAYS));

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

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

        ResponseEntity<Map> response = restTemplate.postForEntity("https://oauth2.googleapis.com/token",
                new HttpEntity<>(body), Map.class);

        return response.getBody().get("access_token").toString();
    }
}
