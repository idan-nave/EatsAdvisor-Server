package com.eatsadvisor.eatsadvisor.config;

import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AppUserRepository appUserRepository;
    private final RefreshTokenService refreshTokenService;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository, 
                         AppUserRepository appUserRepository,
                         RefreshTokenService refreshTokenService) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF because we're using JWTs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No
                                                                                                              // sessions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/auth/**", "/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(new OAuth2LoginSuccessHandler(
                                appUserRepository, refreshTokenService)))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .bearerTokenResolver(new CookieBearerTokenResolver()) // Extract JWT from Cookie
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    /**
     * Custom Bearer Token Resolver to Extract JWT from HttpOnly Cookies
     */
    public static class CookieBearerTokenResolver implements BearerTokenResolver {
        private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

        @Override
        public String resolve(HttpServletRequest request) {
            System.out.println("🔍 CookieBearerTokenResolver: Checking for JWT cookie");
            
            // Log all cookies for debugging
            if (request.getCookies() != null) {
                System.out.println("🍪 Cookies found: " + request.getCookies().length);
                for (Cookie cookie : request.getCookies()) {
                    System.out.println("🍪 Cookie: " + cookie.getName() + " (Domain: " + cookie.getDomain() + ", Path: " + cookie.getPath() + ")");
                    
                    if ("jwt".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        System.out.println("✅ JWT cookie found! Token length: " + token.length());
                        return token;
                    }
                }
            } else {
                System.out.println("❌ No cookies found in request");
            }
            
            // Try header as fallback
            String headerToken = defaultResolver.resolve(request);
            if (headerToken != null) {
                System.out.println("🔄 Using JWT from Authorization header instead");
                return headerToken;
            }
            
            System.out.println("❌ No JWT found in cookies or headers");
            return null;
        }
    }
}
