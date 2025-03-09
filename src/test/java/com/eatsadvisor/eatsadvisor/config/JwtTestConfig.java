package com.eatsadvisor.eatsadvisor.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration for JWT authentication in tests.
 * This configuration provides a mock JWT decoder that returns a valid JWT token
 * with the email claim set to "test@example.com".
 */
@TestConfiguration
public class JwtTestConfig {

    /**
     * Creates a mock JWT decoder that always returns a valid JWT token.
     * The token contains the email claim set to "test@example.com".
     *
     * @return A mock JWT decoder
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> {
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "none");
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "user-id");
            claims.put("email", "test@example.com");
            
            return new Jwt(
                token,
                Instant.now(),
                Instant.now().plusSeconds(300),
                headers,
                claims
            );
        };
    }
}
