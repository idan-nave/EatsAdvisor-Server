package com.eatsadvisor.eatsadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
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
                ClientRegistration.withRegistrationId("google")
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("http://localhost:8080/login/oauth2/code/google")
                        .scope("openid", "profile", "email")
                        .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                        .tokenUri("https://oauth2.googleapis.com/token")
                        .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                        .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                        .userNameAttributeName("email")
                        .build()
        );
    }
}
