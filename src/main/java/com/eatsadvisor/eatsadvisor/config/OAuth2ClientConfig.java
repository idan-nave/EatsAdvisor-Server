package com.eatsadvisor.eatsadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@EnableWebSecurity
public class OAuth2ClientConfig {

    @Value("${app.backend-base-url}")
    private String backendBaseUrl;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.provider.google.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret) {

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            throw new IllegalStateException("Google OAuth credentials missing!");
        }
        // Register Google as an OAuth2 client
        return new InMemoryClientRegistrationRepository(
                ClientRegistration.withRegistrationId("google") // Register OAuth2 client with ID "google"
                        .clientId(clientId) // Set the client ID obtained from Google Developer Console
                        .clientSecret(clientSecret) // Set the client secret obtained from Google Developer Console
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // Use basic
                                                                                                    // authentication
                                                                                                    // method for client
                                                                                                    // authentication
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Use Authorization Code
                                                                                           // grant type for
                                                                                           // authentication
                        .redirectUri(backendBaseUrl + "/login/oauth2/code/google") // Redirect URI after successful
                                                                                   // authentication
                        .scope("openid", "profile", "email") // Request OpenID, profile, and email scopes from Google
                        .authorizationUri(authorizationUri) // Google OAuth2 authorization
                                                                                       // endpoint
                        .tokenUri(tokenUri) // Google OAuth2 token endpoint to exchange
                                                                         // auth code for access token
                        .userInfoUri(userInfoUri) // Endpoint to fetch user
                                                                                         // information from Google
                        .jwkSetUri(jwkSetUri) // Google JWKS URI for validating ID
                                                                                 // tokens
                        .userNameAttributeName("email") // Use the "email" attribute from the user info response as the
                                                        // unique identifier
                        .build() // Build the ClientRegistration object
        );
    }
}
