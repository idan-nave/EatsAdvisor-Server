package com.eatsadvisor.eatsadvisor.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**", "/auth/**", "/oauth2/**", "/login/**").permitAll()
                .requestMatchers("/profile").hasRole("ADMIN")
                .requestMatchers("/profile/{id}").hasRole("ADMIN")
                .requestMatchers("/profile/update").hasRole("ADMIN")
                .requestMatchers("/profile/{id}").hasRole("ADMIN")
                .requestMatchers("/profile/get").permitAll()
                .requestMatchers("/profile/preferences").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> {
            // This is a mock decoder that returns a valid token with the claims from the token
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

    @Bean
    @Primary
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
