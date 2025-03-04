
---

### 📜 **README.md: EatsAdvisor**
```md
# 🍽 EatsAdvisor

### AI-powered meal recommendation system based on menu image analysis.

![EatsAdvisor Banner](https://via.placeholder.com/1200x400?text=EatsAdvisor)

---

## 📖 Overview

**EatsAdvisor** is a smart food recommendation system that utilizes **AI and image processing** to help users make informed meal choices based on menu images. The platform integrates **Spring Boot**, **React (Vite + TypeScript)**, **PostgreSQL (Supabase)**, and **OpenAI** for AI-based menu text extraction and recommendations.

---

## ✨ Features

✅ **AI-Powered Recommendations** - Upload a menu image and get intelligent food recommendations.  
✅ **Google OAuth Authentication** - Secure login using Google OAuth 2.0.  
✅ **Dynamic User Roles** - Separate dashboards for **clients** and **workers**.  
✅ **Supabase Integration** - PostgreSQL as the backend database for structured data storage.  
✅ **Secure REST API** - Built using Spring Boot with **JWT Authentication** and **OAuth2 Security**.  
✅ **Modern Frontend** - Built with React, TypeScript, and React Hook Form for seamless UX.  
✅ **Real-time Updates** - Automatically updates recommendations as users interact.  

---

## 🏗 Tech Stack

### **Frontend (Client)**
- **React (Vite + TypeScript)** - Modern and fast frontend framework.
- **React Router** - Client-side routing for a seamless experience.
- **React Hook Form + Zod** - Form handling and validation.
- **Axios** - API requests to the backend.
- **ESLint & Prettier** - Enforces coding standards.

### **Backend (Server)**
- **Spring Boot (Java 17+)** - REST API development.
- **Spring Security + OAuth2** - Secure authentication & authorization.
- **JPA + Hibernate** - ORM for database interactions.
- **Flyway** - Database migrations.
- **OpenAI API** - AI-based text extraction and recommendations.

### **Database**
- **PostgreSQL (Supabase)** - Cloud database service.
- **Spring Data JPA** - Handles data persistence.

### **DevOps & Security**
- **Docker** - Containerized development.
- **GitHub Actions** - CI/CD automation.
- **JWT Authentication** - Secure API requests.
- **Environment Variables** - Stores sensitive data.

---

## 🚀 Getting Started

### **Prerequisites**
Ensure you have the following installed:
- [Node.js (16+)](https://nodejs.org/)
- [Java 23+](https://openjdk.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/get-started)

---

### **🔧 Installation & Setup**
Clone the repository:
```sh
git clone https://github.com/idan-nave/EatsAdvisor.git
cd EatsAdvisor
```

---

### **Frontend Setup**
```sh
cd Client
npm install
npm run dev
```
Then, visit `http://localhost:5173`.

---

### **Backend Setup**
```sh
cd Server
mvn clean install
mvn spring-boot:run
```
The backend will run on `http://localhost:8080`.

---

### **Environment Variables**
Create a `.env` file in both **Client** and **Server** folders.

#### **Client (`Client/.env`)**
```ini
VITE_API_URL=http://localhost:8080/api
VITE_OPENAI_KEY=your-openai-api-key
VITE_SUPABASE_URL=your-supabase-url
VITE_SUPABASE_KEY=your-supabase-key
```

#### **Server (`Server/.env`)**
```ini
DB_URL=jdbc:postgresql://your-supabase-url:5432/postgres
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
JWT_SECRET=your-jwt-secret
OPENAI_API_KEY=your-openai-api-key
```

---

## 🔗 API Documentation

### **Authentication**
#### 🔹 Google OAuth Login
```http
POST /auth/login
```
_Response:_
```json
{
  "token": "ey123...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### **Menu Upload**
#### 🔹 Upload Menu Image
```http
POST /menu/upload
```
_Request:_
```json
{
  "image": "base64-encoded-image"
}
```
_Response:_
```json
{
  "menuId": 5,
  "analyzedText": "Spaghetti Carbonara - $12.99"
}
```

### **Recommendations**
#### 🔹 Get AI-Powered Recommendations
```http
GET /recommendations/{menuId}
```
_Response:_
```json
{
  "suggestions": [
    "Spaghetti Carbonara",
    "Vegetarian Pizza",
    "Caesar Salad"
  ]
}
```

---

## 🔒 Security Considerations

✅ **Uses OAuth2 & JWT Authentication** - Protects API endpoints.  
✅ **Environment Variables** - Prevents API key leaks.  
✅ **OWASP Top 10 Protection** - Mitigates XSS, CSRF, SQL Injection.  
✅ **Spring Security Roles** - Restricts access to authorized users.  

---

## 🛠 Docker Support

To run the project using **Docker**, use the following commands:

```sh
docker-compose up --build
```

This will start:
- Backend (`Spring Boot`)
- Frontend (`Vite`)
- PostgreSQL (`Supabase`)

---

## 🤝 Contributing

1. **Fork** the repository.
2. **Create a branch**: `git checkout -b feature-name`
3. **Commit changes**: `git commit -m "Add new feature"`
4. **Push the branch**: `git push origin feature-name`
5. **Create a Pull Request**.

---

## 🎯 Roadmap

✅ **Phase 1**: Initial setup (Frontend, Backend, OAuth2)  
✅ **Phase 2**: AI-based recommendations (OpenAI integration)  
🔜 **Phase 3**: Worker dashboard & task management  
🔜 **Phase 4**: Mobile-friendly UI  

---

## 📜 License

This project is licensed under the **MIT License**.

---

## 📬 Contact

For support or inquiries:
- **GitHub:** [@idan-nave](https://github.com/idan-nave)
- **GitHub:** [@zeev-joseph](https://github.com/zeev-joseph)
```

---

### Frontend Structure
The frontend, built with Vite, React, and TypeScript, should have a structured layout to manage UI components, API calls, and authentication. Here's a breakdown:

- **Root Level:** Include `public/` for static assets like `index.html`, and configuration files like `vite.config.ts`, `package.json`, and `tsconfig.json`.
- **Source Directory (`src/`):**
  - `assets/`: Store images and styles.
  - `components/`: House reusable UI elements like `Header.tsx`, `Footer.tsx`, `MenuUploader.tsx`, and `RecommendationCard.tsx`.
  - `hooks/`: Contain custom hooks such as `useAuth.ts`, `useForm.ts`, and `useApi.ts` for managing state and API interactions.
  - `pages/`: Define routes like `Home.tsx`, `Login.tsx`, `Register.tsx`, and `Profile.tsx`.
  - `services/`: Manage API calls with files like `auth.ts` and `menu.ts`.
  - `types/`: Define TypeScript types for data models, such as `menu.ts`, `user.ts`, and `recommendation.ts`.
  - `utils/`: Include utility functions and validation schemas, e.g., `validation.ts` with Zod schemas.
  - Key entry points: `App.tsx` for the main component and `index.tsx` for the application root.

This structure ensures easy navigation and scalability, especially with authentication handled via JWT for basic scenarios and next-auth with Google OAuth for advanced cases.

---

### Backend Structure
The backend, using Spring Boot with Java, follows the MVC pattern for a robust architecture. Here's how it should be organized:

- **Root Level:** Include configuration files like `pom.xml` (if using Maven) and test directories.
- **Source Directory (`src/main/`):**
  - Package structure under `java/com/eatsadvisor/`:
    - `controllers/`: Handle HTTP requests, e.g., `MenuController.java`, `AuthController.java`.
    - `services/`: Manage business logic, such as `MenuService.java`, `RecommendationService.java`, and `UserService.java`.
    - `repositories/`: Define database operations, like `MenuRepository.java` and `UserRepository.java`, using JPA.
    - `models/`: Contain entity classes with normalized relationships, e.g., `Menu.java`, `User.java`, `Recommendation.java`.
    - `config/`: Include configurations for database (`DatabaseConfig.java`), security (`SecurityConfig.java`), and OpenAI (`OpenAIConfig.java`).
    - `utils/`: House utilities like `OpenAIUtils.java` for API interactions.
    - `exceptions/`: Define custom exceptions for error handling.
    - Main application file: `EatsAdvisorApplication.java`.
  - `src/test/`: Store JUnit tests for controllers and services, e.g., `MenuControllerTest.java`, `MenuServiceTest.java`.
  - `resources/`: Contain configuration files like `application.yml` for Spring Boot settings.

This setup ensures the backend integrates seamlessly with Supabase for PostgreSQL and handles AI functionalities via OpenAI, with security measures against OWASP Top 10 vulnerabilities like SQL injection.

---

---

### Survey Note: Detailed Analysis of EatsAdvisor App Structure

The EatsAdvisor application, designed to assist users in selecting meals based on uploaded menu photos and personal preferences, requires a well-organized file and folder structure for both its frontend (client) and backend (server). This note provides a comprehensive breakdown, ensuring scalability, maintainability, and adherence to the specified tech stack, including Vite, React, TypeScript for the frontend, and Spring Boot with Java for the backend, integrated with OpenAI and Supabase.

#### Frontend: Client-Side Architecture

The frontend, built using Vite, React, and TypeScript, leverages a component-based architecture with custom hooks, aligning with modern React development practices. The structure is as follows:

- **Public Directory:**
  - Contains static assets such as `index.html`, favicon, and other resources accessible at the root level.
  - Essential for serving the initial HTML file and static files during development and production.

- **Source Directory (`src/`):**
  - **Assets (`assets/`):** Houses images, CSS files, and other static resources. For example, styles for the application can be stored here, ensuring easy access for components.
  - **Components (`components/`):** Includes reusable UI elements critical for the application's interface. Examples include:
    - `Header.tsx`: Manages the top navigation bar.
    - `Footer.tsx`: Handles the bottom section of pages.
    - `MenuUploader.tsx`: Facilitates menu photo uploads, a core feature for user interaction.
    - `RecommendationCard.tsx`: Displays AI-generated meal recommendations, enhancing user experience.
  - **Hooks (`hooks/`):** Contains custom React hooks for managing state and side effects. Key files include:
    - `useAuth.ts`: Handles authentication state, supporting both JWT and next-auth with Google OAuth.
    - `useForm.ts`: Integrates with react-hook-form for form management, ensuring robust form handling.
    - `useApi.ts`: Manages API calls to the backend, facilitating communication for menu uploads and recommendations.
  - **Pages (`pages/`):** Defines routes for different views, using React Router for navigation. Examples include:
    - `Home.tsx`: The landing page for users.
    - `Login.tsx`: Handles user login, integrating with authentication mechanisms.
    - `Register.tsx`: Manages user registration.
    - `Profile.tsx`: Displays user profile information, including preferences and history.
  - **Services (`services/`):** Manages API interactions, crucial for backend communication. Files include:
    - `auth.ts`: Handles authentication-related API calls, such as login and logout.
    - `menu.ts`: Manages menu upload and recommendation retrieval, interfacing with the backend.
  - **Types (`types/`):** Defines TypeScript interfaces for data models, ensuring type safety. Examples include:
    - `menu.ts`: Types for menu data, such as items and prices.
    - `user.ts`: Types for user data, including preferences and allergies.
    - `recommendation.ts`: Types for AI-generated recommendations.
  - **Utils (`utils/`):** Contains utility functions and validation schemas. For instance:
    - `validation.ts`: Uses Zod for schema validation, ensuring data integrity in forms.
    - `helpers.ts`: Includes helper functions for various tasks, enhancing code reusability.
  - **Entry Points:** 
    - `App.tsx`: The main application component, orchestrating routing and layout.
    - `index.tsx`: The root component, initializing the React application.

- **Configuration Files:**
  - `vite.config.ts`: Configures Vite for development and build processes.
  - `package.json`: Lists dependencies, including react, react-dom, react-hook-form, zod, and next-auth for authentication.
  - `tsconfig.json`: Sets TypeScript configuration for type checking and compilation.

This structure ensures the frontend is modular, with clear separation of concerns, supporting authentication via JWT for basic scenarios and next-auth with Google OAuth for advanced cases, enhancing user experience and security.

#### Backend: Server-Side Architecture

The backend, developed with Spring Boot and Java, follows the Model-View-Controller (MVC) pattern, ensuring a robust and scalable architecture. It integrates with Supabase for managed PostgreSQL and OpenAI for AI functionalities. The structure is as follows:

- **Source Directory (`src/main/`):**
  - **Package Structure (`java/com/eatsadvisor/`):**
    - **Controllers (`controllers/`):** Handle HTTP requests and responses, forming the "C" in MVC. Examples include:
      - `MenuController.java`: Manages endpoints for menu uploads and retrieval.
      - `AuthController.java`: Handles authentication endpoints, such as login and registration.
    - **Services (`services/`):** Encapsulate business logic, the "M" in MVC. Key files include:
      - `MenuService.java`: Processes menu-related operations, including storage and retrieval.
      - `RecommendationService.java`: Integrates with OpenAI for generating meal recommendations based on user preferences.
      - `UserService.java`: Manages user-related operations, such as profile updates and preference settings.
    - **Repositories (`repositories/`):** Define database operations using JPA, the "M" in MVC. Examples include:
      - `MenuRepository.java`: Handles CRUD operations for menu data.
      - `UserRepository.java`: Manages user data, ensuring normalized relationships.
    - **Models (`models/`):** Represent entity classes with normalized relationships, ensuring database integrity. Examples include:
      - `Menu.java`: Entity for menu items, with fields like name, price, and description.
      - `User.java`: Entity for user data, including email, preferences, allergies, and portion size preferences.
      - `Recommendation.java`: Entity for AI-generated recommendations, linking to menus and users.
    - **Config (`config/`):** Contains configuration classes for various services. Files include:
      - `DatabaseConfig.java`: Configures JPA and PostgreSQL connection to Supabase.
      - `SecurityConfig.java`: Sets up security, including JWT validation for protected endpoints.
      - `OpenAIConfig.java`: Configures OpenAI API integration, providing API keys and client setup.
    - **Utils (`utils/`):** Houses utility classes for additional functionality. For example:
      - `OpenAIUtils.java`: Manages API calls to OpenAI, processing menu images and generating recommendations.
    - **Exceptions (`exceptions/`):** Defines custom exceptions for error handling, enhancing robustness.
    - **Main Application (`EatsAdvisorApplication.java`):** The Spring Boot entry point, initializing the application.

- **Test Directory (`src/test/`):**
  - Contains JUnit tests for backend components, ensuring functionality and reliability. Examples include:
    - `MenuControllerTest.java`: Tests controller endpoints.
    - `MenuServiceTest.java`: Validates service logic.

- **Resources Directory (`resources/`):**
  - Includes configuration files like `application.yml` or `application.properties`, setting up database connections, security, and other Spring Boot configurations. For instance:
    - Database URL for Supabase, ensuring normalized schema with proper relationships.

This structure ensures the backend is secure, with measures against OWASP Top 10 vulnerabilities like SQL injection, handled via parameterized queries in JPA. The integration with OpenAI involves processing menu photos, likely using GPT-4 Vision for text extraction and recommendation generation, based on user preferences stored in the database.

#### Integration and Security Considerations

- **Database Normalization with Supabase:** The models should be designed with normalized relationships, avoiding redundancy. For example, a `User` entity might have a one-to-many relationship with `Recommendation`, ensuring data integrity and efficiency.
- **OpenAI Integration:** The backend uses OpenAI API, particularly GPT-4 Vision, to process menu images. This involves extracting text from photos and generating recommendations, requiring `OpenAIUtils.java` to handle API calls, potentially using base64 encoding for image data.
- **Security:** Implements JWT for authentication, with `SecurityConfig.java` validating tokens. OWASP Top 10, especially SQL injection, is mitigated by using parameterized queries in JPA, ensuring secure database operations.

#### Testing Strategy

- Unit tests with JUnit for backend services and controllers ensure functionality, with examples like testing `MenuService` for menu processing and `AuthController` for authentication endpoints.

#### Unexpected Detail: OpenAI Image Processing

An interesting aspect is the use of OpenAI for image processing, particularly with GPT-4 Vision, which can handle menu photo analysis. This capability, not immediately obvious, enhances the application's AI-driven recommendations, potentially surprising users expecting traditional text-based inputs.

#### Tables for Clarity

Below is a table summarizing the key directories and their purposes for both frontend and backend:

| **Directory**         | **Purpose**                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| `public/` (Frontend)  | Stores static assets like `index.html` for initial page load.               |
| `src/assets/`         | Holds images and styles for the application.                                |
| `src/components/`     | Contains reusable UI components like `MenuUploader.tsx`.                    |
| `src/hooks/`          | Manages custom React hooks for state and API interactions.                  |
| `src/main/java/com/eatsadvisor/controllers/` | Handles HTTP requests, forming the controller layer in MVC.                 |
| `src/main/java/com/eatsadvisor/services/` | Encapsulates business logic, the service layer in MVC.                      |
| `src/main/java/com/eatsadvisor/models/` | Defines entity classes with normalized relationships for database.          |
| `src/test/java/com/eatsadvisor/` | Houses JUnit tests for backend components, ensuring reliability.            |

This table aids in visualizing the organizational structure, enhancing understanding for developers.

In conclusion, this detailed structure ensures the EatsAdvisor app is well-prepared for development, with clear separation of concerns, robust security, and integration with modern AI capabilities, ready for further implementation by the development team.

#### Key Citations
- [Extracting Structured Data from Images Using OpenAI’s gpt-4-vision](https://medium.com/@foxmike/extracting-structured-data-from-images-using-openais-gpt-4-vision-and-jason-liu-s-instructor-ec7f54ee0a91)
- [Spring Boot with PostgreSQL Guide](https://spring.io/guides/gs/relational-data-access/)
- [Vite React TypeScript Setup](https://vitejs.dev/guide/#scaffolding-your-first-react-app)
- [next-auth Authentication for React](https://next-auth.js.org/adapters)
- [Supabase JavaScript Initialization](https://supabase.com/docs/reference/javascript/initialize)


## 🔒 Security Considerations

✅ **Uses OAuth2 & JWT Authentication** - Protects API endpoints.  
✅ **Environment Variables** - Prevents API key leaks.  
✅ **OWASP Top 10 Protection** - Mitigates XSS, CSRF, SQL Injection.  
✅ **Spring Security Roles** - Restricts access to authorized users.  


---

### **✅  OAuth Flow **
1. **Users log in with Google** (OAuth Client).
2. **Spring Boot Backend validates Google’s JWT tokens** (OAuth Resource Server).
3. **Backend provides role-based access control (RBAC)** for users vs. workers.
4. **Users upload menu photos** → API processes images → OpenAI API analyzes them.
5. **Workers receive orders** based on client choices.
6. **Secure API with OWASP best practices** (CSRF, XSS, SQL Injection, etc.).

---

## **🔹 What Spring Extensions to Use?**
| **Extension** | **Why You Need It?** |
|--------------|----------------------|
| **Spring Security** | Protects APIs & handles authentication |
| **OAuth Client** | Allows users to log in with Google |
| **OAuth Resource Server** | Validates Google JWTs for secure API access |

**No need of Spring Authorization Server** because Google will issue JWT tokens.

---

## **🛠️ Implementation Steps**
### **1️⃣ Add Dependencies to `pom.xml`**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

### **2️⃣ Configure Google OAuth in `application.yml`**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope: openid, profile, email
```

---

### **3️⃣ Enable OAuth in Spring Security (JWT Only)**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // REST API - No CSRF
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll() // Public endpoints
                .requestMatchers("/users/**").authenticated()
                .requestMatchers("/workers/**").hasRole("WORKER")
                .anyRequest().authenticated()
            )
            .oauth2Login(withDefaults()) // Enable OAuth login
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt); // Validate JWT tokens

        return http.build();
    }
}
```

---

### **4️⃣ Configure JWT Validation (Google’s Public Keys)**
Google issues **JWT tokens** that your backend must verify.  
Google’s **JWKS endpoint** (`https://www.googleapis.com/oauth2/v3/certs`) provides public keys to validate these tokens.

```java
@Bean
public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
}
```

---

### **5️⃣ Implement Role-Based Access Control (RBAC)**
Since your **workers and users** have different permissions:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/users/**").authenticated()
            .requestMatchers("/workers/**").hasAuthority("ROLE_WORKER")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

    return http.build();
}
```

---

## **🔐 Security (OWASP Top 10 Best Practices)**
| **Vulnerability** | **How to Prevent** |
|------------------|-------------------|
| **XSS (Cross-Site Scripting)** | Use **Spring's `@Valid` & input validation**, sanitize inputs |
| **SQL Injection** | Use **Spring Data JPA / Hibernate** instead of raw SQL queries |
| **CSRF (Cross-Site Request Forgery)** | **Disable CSRF for APIs (`http.csrf().disable()`)** |
| **Broken Authentication** | **Use OAuth 2.0 and JWTs** instead of sessions |
| **Security Misconfigurations** | Use **HTTPS** and secure JWT storage |
| **Insufficient Logging** | Log all authentication & API events securely |

---

## **🚀 TL;DR - Your OAuth & Security Plan**
1️⃣ **Use Google for authentication** (`OAuth Client`).  
2️⃣ **Validate Google JWTs** on the backend (`OAuth Resource Server`).  
3️⃣ **Role-based access control (RBAC)**: Clients vs. Workers.  
4️⃣ **Security hardening (OWASP best practices)**.  
5️⃣ **All authentication happens via JWTs (stateless, no sessions/cookies).**  

