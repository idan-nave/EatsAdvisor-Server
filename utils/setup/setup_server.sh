#!/bin/bash

# Exit on error
set -e

echo "🔧 Setting up EatsAdvisor Server..."

cd Server

# Step 1: Create .env file (if not exists)
if [ ! -f .env ]; then
    echo "📄 Creating .env file..."
    cat > .env <<EOL
DB_URL=jdbc:postgresql://your-supabase-url:5432/postgres
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
JWT_SECRET=your-jwt-secret
OPENAI_API_KEY=your-openai-api-key
EOL
fi

# Step 3: Update `application.properties` -> `application.yml`
echo "📄 Creating application.yml..."
rm -f src/main/resources/application.properties
cat > src/main/resources/application.yml <<EOL
server:
  port: 8080

spring:
  datasource:
    url: \${DB_URL}
    username: \${DB_USERNAME}
    password: \${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://accounts.google.com
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs
EOL

# Step 4: Create database configuration
echo "⚙️ Creating DatabaseConfig.java..."
mkdir -p src/main/java/com/eatsadvisor/eatsadvisor/config
cat > src/main/java/com/eatsadvisor/eatsadvisor/config/DatabaseConfig.java <<EOL
package com.eatsadvisor.eatsadvisor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

@Configuration
public class DatabaseConfig {

    @Value("\${spring.datasource.url}")
    private String dbUrl;

    @Value("\${spring.datasource.username}")
    private String dbUsername;

    @Value("\${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }
}
EOL

# Step 5: Create Models
echo "🗂 Creating Models..."
mkdir -p src/main/java/com/eatsadvisor/eatsadvisor/models

# User Model
cat > src/main/java/com/eatsadvisor/eatsadvisor/models/User.java <<EOL
package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;
}

enum Role {
    CLIENT, WORKER
}
EOL

# Menu Model
cat > src/main/java/com/eatsadvisor/eatsadvisor/models/Menu.java <<EOL
package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String analyzedText;
}
EOL

# Recommendation Model
cat > src/main/java/com/eatsadvisor/eatsadvisor/models/Recommendation.java <<EOL
package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Menu menu;

    private String suggestedItem;
}
EOL

# Step 6: Create Repositories
echo "🛠 Creating Repositories..."
mkdir -p src/main/java/com/eatsadvisor/eatsadvisor/repositories

cat > src/main/java/com/eatsadvisor/eatsadvisor/repositories/UserRepository.java <<EOL
package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
EOL

# Step 7: Create Services
echo "⚙ Creating Services..."
mkdir -p src/main/java/com/eatsadvisor/eatsadvisor/services

cat > src/main/java/com/eatsadvisor/eatsadvisor/services/UserService.java <<EOL
package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.User;
import com.eatsadvisor.eatsadvisor.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
EOL

# Step 8: Create Controllers
echo "📡 Creating Controllers..."
mkdir -p src/main/java/com/eatsadvisor/eatsadvisor/controllers

cat > src/main/java/com/eatsadvisor/eatsadvisor/controllers/UserController.java <<EOL
package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.User;
import com.eatsadvisor.eatsadvisor.services.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{email}")
    public Optional<User> getUserByEmail(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }
}
EOL

