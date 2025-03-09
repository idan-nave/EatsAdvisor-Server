package com.eatsadvisor.eatsadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class OAuth2ClientConfig {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${GOOGLE_OAUTH_CLIENT_ID}") String clientId,
            @Value("${GOOGLE_OAUTH_CLIENT_SECRET}") String clientSecret) {

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            throw new IllegalStateException("Google OAuth credentials missing!");
        }
        // Register Google as an OAuth2 client
        return new InMemoryClientRegistrationRepository(
                ClientRegistration.withRegistrationId("google") // Register OAuth2 client with ID "google"
                        .clientId(clientId) // Set the client ID obtained from Google Developer Console
                        .clientSecret(clientSecret) // Set the client secret obtained from Google Developer Console
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // Use basic authentication method for client authentication
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Use Authorization Code grant type for authentication
                        .redirectUri("http://localhost:8080/login/oauth2/code/google") // Redirect URI after successful authentication
                        .scope("openid", "profile", "email") // Request OpenID, profile, and email scopes from Google
                        .authorizationUri("https://accounts.google.com/o/oauth2/auth") // Google OAuth2 authorization endpoint
                        .tokenUri("https://oauth2.googleapis.com/token") // Google OAuth2 token endpoint to exchange auth code for access token
                        .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo") // Endpoint to fetch user information from Google
                        .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs") // Google JWKS URI for validating ID tokens
                        .userNameAttributeName("email") // Use the "email" attribute from the user info response as the unique identifier
                        .build() // Build the ClientRegistration object
        );
    }
}
