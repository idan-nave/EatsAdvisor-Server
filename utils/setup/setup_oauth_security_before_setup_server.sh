#!/bin/bash

# Define base directory
BASE_DIR="src/main/java/com/eatsadvisor/eatsadvisor"

echo "📁 Creating necessary directories..."
mkdir -p $BASE_DIR/config
mkdir -p $BASE_DIR/controllers
mkdir -p $BASE_DIR/services
mkdir -p $BASE_DIR/models
mkdir -p $BASE_DIR/repositories

echo "🔧 Adding OAuth Security Configuration..."
cat <<EOF > $BASE_DIR/config/SecurityConfig.java
package com.eatsadvisor.eatsadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless JWT auth
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/users/**").authenticated()
                .requestMatchers("/workers/**").hasAuthority("ROLE_WORKER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // Prefix for role-based access

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
EOF

echo "🔧 Adding JWT Decoder Configuration..."
cat <<EOF > $BASE_DIR/config/JwtDecoderConfig.java
package com.eatsadvisor.eatsadvisor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
    }
}
EOF

echo "📜 Creating UserController..."
cat <<EOF > $BASE_DIR/controllers/UserController.java
package com.eatsadvisor.eatsadvisor.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/users")
public class UserController {
    @GetMapping("/profile")
    public String getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        return "User email: " + jwt.getClaim("email");
    }
}
EOF

echo "📜 Creating WorkerController..."
cat <<EOF > $BASE_DIR/controllers/WorkerController.java
package com.eatsadvisor.eatsadvisor.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/workers")
public class WorkerController {
    @GetMapping("/tasks")
    public String getWorkerTasks(@AuthenticationPrincipal Jwt jwt) {
        return "Worker email: " + jwt.getClaim("email") + " - Assigned orders retrieved!";
    }
}
EOF

echo "📜 Updating application.properties..."
cat <<EOF > src/main/resources/application.properties
server.port=8080

spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid, profile, email

spring.security.oauth2.resourceserver.jwt.issuer-uri=https://accounts.google.com
EOF

echo "📦 Ensuring dependencies in pom.xml..."
POM_FILE="pom.xml"
if ! grep -q "<artifactId>spring-boot-starter-oauth2-client</artifactId>" "$POM_FILE"; then
    sed -i '/<dependencies>/a \
        <dependency>\n\
            <groupId>org.springframework.boot</groupId>\n\
            <artifactId>spring-boot-starter-oauth2-client</artifactId>\n\
        </dependency>\n\
        <dependency>\n\
            <groupId>org.springframework.boot</groupId>\n\
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>\n\
        </dependency>\n\
        <dependency>\n\
            <groupId>org.springframework.boot</groupId>\n\
            <artifactId>spring-boot-starter-security</artifactId>\n\
        </dependency>' "$POM_FILE"
fi

echo "🚀 Setup complete!"
echo "🔹 Start your server with: mvn spring-boot:run"
echo "🔹 Access APIs: "
echo "   - Login with Google: http://localhost:8080/oauth2/authorization/google"
echo "   - User Profile: http://localhost:8080/users/profile (Requires JWT)"
echo "   - Worker Tasks: http://localhost:8080/workers/tasks (Requires JWT & Worker Role)"
