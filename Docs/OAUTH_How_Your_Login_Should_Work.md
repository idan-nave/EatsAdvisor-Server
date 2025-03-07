### **✅ How Your Login Should Work (Step-by-Step)**
We are using **OAuth2 Authorization Code Flow + OIDC + Refresh Tokens**, which means:

1️⃣ **User enters the site (`http://localhost:3000/`)**  
   - If **a valid JWT exists in cookies**, authenticate **without redirecting to Google**.
   - If **only a refresh token exists**, request a new access token **without redirecting to Google**.
   - If **no valid JWT or refresh token exists**, the user must **manually press "Login"**.

2️⃣ **User clicks "Login"**  
   - Redirect to **Google OAuth login** (`https://accounts.google.com/o/oauth2/auth`).
   - User logs in with Google.
   - Google **redirects back to your backend** (`http://localhost:8080/login/oauth2/code/google`).
   - Backend:
     - Validates the **OAuth2 Code** and exchanges it for an **ID Token + Access Token**.
     - Creates a **refresh token** (stored in a **HttpOnly cookie**).
     - Redirects the user to `http://localhost:3000/login-success`.

3️⃣ **Frontend Handles Login Success (`/login-success`)**  
   - Fetch **user data** (`GET /api/users/me`).
   - If authenticated, **redirect to dashboard** (`/dashboard`).
   - If not authenticated, **redirect to homepage (`/`)**.

---

## **🔹 What You Need to Implement**
1. **A Login Page (`/login`)** → User clicks "Login"
2. **Handle `/login-success` to fetch user info** (so login completes)
3. **Auto-login using JWT or refresh token** (if available)

---

## **🛠 1️⃣ Implement the Login Page (`/login`)**
Create a new page in your React app:  

🔹 **`src/pages/Login.tsx`**
```tsx
import { useEffect } from "react";
import { useAuth } from "../hooks/useAuth";

export default function Login() {
  const { login } = useAuth();

  useEffect(() => {
    // If the user manually enters /login, redirect to OAuth login
    login();
  }, []);

  return (
    <div>
      <h2>Redirecting to Login...</h2>
    </div>
  );
}
```
### **🔹 What this does:**
- When a user **enters `/login`**, it **automatically calls `login()`**.
- `login()` redirects the user to Google OAuth.
- There is **no login form**, since Google handles authentication.

---

## **🛠 2️⃣ Modify `App.tsx` to Include the Login Page**
Update **React Router** to include the new `/login` page:

🔹 **`src/App.tsx`**
```tsx
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import { useAuth } from "./hooks/useAuth";

export default function App() {
  const { user } = useAuth();

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />  {/* ✅ Login Page */}
        <Route path="/dashboard" element={user ? <Dashboard /> : <Navigate to="/login" />} />
        <Route path="/login-success" element={<Navigate to="/dashboard" />} /> {/* ✅ Redirect after login */}
      </Routes>
    </Router>
  );
}
```
### **🔹 What this does:**
- If **not logged in**, users will **be sent to `/login`**.
- After login, they will be **redirected to `/dashboard`**.
- If `/login-success` is hit, **it redirects to `/dashboard`**.

---

## **🛠 3️⃣ Fix `useAuth.ts` to Handle JWT & Refresh Tokens**
🔹 **`src/hooks/useAuth.ts`**
```tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import { User } from "../types/user";

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuthentication = async () => {
      try {
        const response = await api.get("/api/users/me");  // ✅ Fetch user if JWT exists
        setUser(response.data);
      } catch (error) {
        console.error("User not authenticated:", error);
        setUser(null);
      }
    };

    checkAuthentication();  // ✅ Check authentication on load
  }, []);

  const login = () => {
    window.location.href = `${api.defaults.baseURL}/oauth2/authorization/google`; // ✅ Redirect to Google
  };

  const logout = async () => {
    await api.get("/auth/logout");
    setUser(null);
    navigate("/");
  };

  return { user, login, logout };
}
```
### **🔹 What this does:**
- **On page load**, it tries to fetch the user:
  - ✅ If JWT is valid → User is set
  - ❌ If JWT is missing or expired → No user (must log in)
- **Login** → Redirects to **Google OAuth**.
- **Logout** → Clears session and redirects.

---

## **🛠 4️⃣ Handle Login Success (`/login-success`)**
After Google login, the user is redirected to `/login-success`.  
This should **fetch the user data and move to the dashboard**.

🔹 **`src/pages/LoginSuccess.tsx`**
```tsx
import { useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function LoginSuccess() {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      navigate("/dashboard"); // ✅ Redirect once user data is loaded
    }
  }, [user, navigate]);

  return <h2>Logging in...</h2>;
}
```
### **🔹 What this does:**
- When the user **lands on `/login-success`**, it checks authentication.
- If authenticated, **redirect to `/dashboard`**.
- If not, it waits until authentication completes.

---

## **🔹 Final Overview**
| **Action** | **What Happens** |
|------------|----------------|
| **User enters the site** (`/`) | ✅ If JWT exists → Auto-login. <br> ❌ If no JWT → Show login button. |
| **User clicks "Login"** | 🔀 Redirect to Google OAuth. |
| **Google Redirects to Backend (`/login/oauth2/code/google`)** | 🖥️ Backend validates OAuth2 code, sets cookies, and redirects to `/login-success`. |
| **User lands on `/login-success`** | 🔄 Fetches user data → Redirect to `/dashboard`. |
| **User lands on `/dashboard`** | 🎉 Displays user info (if authenticated). |

---

## **🚀 Next Steps**
1. ✅ **Implement the changes** (Login page, authentication flow, React Router updates).
2. ✅ **Test Login & Logout:**
   - Check if JWT is used properly.
   - Try logging in and out multiple times.
   - Ensure refresh token logic works.
3. 🔄 **Check Edge Cases:**
   - If the JWT is **expired**, does it request a refresh token?
   - What happens if the user **deletes cookies**?

---

### **🔹 Final Thoughts**
- The **login should happen only when the user presses the button**, **not automatically**.  
- If a **valid JWT or refresh token exists**, login should happen **without user interaction**.  
- **Google handles authentication**, but your app needs to **fetch user data after login**.

Try these fixes, and let me know how it works! 🚀

## **🔹 Full Backend Implementation: Authorization Code Flow & OIDC & Refresh Tokens**
This implementation follows the **most secure** OAuth 2.0 flow: **Authorization Code Flow with OIDC and Refresh Tokens**.

---

## **📌 Backend Code Breakdown**
We'll implement the **full authentication flow step-by-step**.

### **🔹 How It Works (Step-by-Step)**

1️⃣ **User initiates login** (e.g., "Sign in with Google").  
2️⃣ **Redirect to OAuth Provider** → The user is sent to an **authorization URL** (Google, Microsoft, etc.).  
3️⃣ **User authenticates & grants permissions** → Logs in and consents to requested scopes.  
4️⃣ **OAuth Provider redirects user back with an Authorization Code**.  
5️⃣ **Backend exchanges the Authorization Code for tokens** (via a secure request).
- Receives **ID Token (JWT) → For authentication**.
- Receives **Access Token → For API access** (optional).  
  6️⃣ **Backend verifies ID Token & matches the user in the database**.  
  7️⃣ **Session is created, and the user is authenticated**.


---
### **Full Backend Implementation for OAuth 2.0 Authorization Code Flow with OIDC & Refresh Tokens**

---

### **1️⃣ User Initiates Login**
- The frontend triggers the login by redirecting the user to the OAuth provider (e.g., Google).


---

### **🔹 How Does the Backend Handle  (implicit)  `/oauth2/authorization/google`?**
**not explicitly** through a controller like `@GetMapping("/login")`. Instead, it's handled automatically by **Spring Security’s OAuth2 implementation**.
📌 **File:** `SecurityConfig.java`
```java
.oauth2Login(oauth2 -> oauth2
    .successHandler((request, response, authentication) -> {
        response.sendRedirect("/auth/login-success");
    })
)
```
### **🔹 How is it Registered?**
Spring Security knows about **Google as an OAuth2 provider** because we defined it in:

📌 **File:** `OAuth2ClientConfig.java`
Because of this, Spring Security automatically registers the following endpoints:
    /oauth2/authorization/google → Redirects user to Google login page
    /login/oauth2/code/google → Handles the Authorization Code callback from Google


📌 **Then What Happens?**
- The backend sends the user to the Google OAuth2 authentication page.


📌 **Example Redirected URL**
```
https://accounts.google.com/o/oauth2/auth?
  response_type=code
  &client_id=YOUR_CLIENT_ID
  &redirect_uri=http://localhost:8080/login/oauth2/code/google
  &scope=openid%20email%20profile
```

---

### **2️⃣ OAuth Provider Redirects User with Authorization Code**
After the user logs in, Google redirects back to our server with an **Authorization Code**.

📌 **Example Google Response (Redirects to Backend)**
```
GET /login/oauth2/code/google?code=4%2F0AfJohX...
```

📌 **Backend Receives Request in Security Configuration**
```java
.oauth2Login(oauth2 -> oauth2
    .successHandler((request, response, authentication) -> {
        response.sendRedirect("/auth/login-success");
    })
)
```
📌 **File:** `SecurityConfig.java`  
📌 **What Happens?**
- The backend detects the redirect from Google and forwards it to the `/auth/login-success` handler.

---

### **3️⃣ Backend Exchanges Authorization Code for Tokens**
The backend makes a **secure POST request** to exchange the code for **ID Token (JWT)** & **Access Token**.

📌 **Example Backend Request to Google (implicit) **
```java
public String exchangeCodeForTokens(String authorizationCode) {
    RestTemplate restTemplate = new RestTemplate();
    Map<String, String> body = Map.of(
        "client_id", googleClientId,
        "client_secret", googleClientSecret,
        "code", authorizationCode,
        "grant_type", "authorization_code",
        "redirect_uri", "http://localhost:8080/login/oauth2/code/google"
    );

    ResponseEntity<Map> response = restTemplate.postForEntity(
        "https://oauth2.googleapis.com/token", new HttpEntity<>(body), Map.class
    );

    return response.getBody().get("id_token").toString();
}
```
📌 **File:** `OAuth2Service.java`  
📌 **Example Google Response**
```json
{
  "access_token": "ya29.A0Af...",
  "expires_in": 3600,
  "refresh_token": "1//04i...",
  "scope": "openid email profile",
  "id_token": "eyJhbGciOiJSUzI1NiIs..."
}
```
📌 **What Happens?**
- Backend **sends authorization code** → gets **ID Token & Refresh Token**.

---

### **4️⃣ Backend Verifies ID Token & Matches User in Database**
The backend extracts user details from the **ID Token (JWT)** and checks if the user exists.

📌 **Example JWT Decoding**
```java
public Map<String, Object> decodeJwt(String idToken) {
    Jwt decodedToken = JwtHelper.decode(idToken);
    return new ObjectMapper().readValue(decodedToken.getClaims(), Map.class);
}
```
📌 **File:** `JwtService.java`  
📌 **Decoded JWT Payload**
```json
{
  "sub": "116809698680277214658",
  "email": "eatsadvisor@gmail.com",
  "name": "EatsAdvisor",
  "picture": "https://lh3.googleusercontent.com/a/...",
  "iss": "https://accounts.google.com"
}
```

📌 **Find or Create User in DB**
```java
public AppUser findOrCreateUser(Map<String, Object> claims) {
    String email = (String) claims.get("email");

    return userRepository.findByEmail(email).orElseGet(() -> {
        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setOauthProvider("google");
        newUser.setOauthProviderId((String) claims.get("sub"));
        return userRepository.save(newUser);
    });
}
```
📌 **File:** `AppUserService.java`  
📌 **What Happens?**
- Extracts **email & sub (OAuth ID)**.
- **Finds or creates** user in the database.

---

### **5️⃣ Backend Issues Refresh Token & Stores It**
After login, the backend generates and stores a **refresh token**.

📌 **Generate Refresh Token**
```java
public String createRefreshToken(Long userId) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUserId(userId);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setExpiry(Instant.now().plus(7, ChronoUnit.DAYS));

    refreshTokenRepository.save(refreshToken);
    return refreshToken.getToken();
}
```
📌 **File:** `RefreshTokenService.java`  
📌 **What Happens?**
- Stores a **secure refresh token** in the DB.

📌 **Set Refresh Token as HttpOnly Cookie**
```java
Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
refreshCookie.setHttpOnly(true);
refreshCookie.setSecure(true);
refreshCookie.setPath("/");
refreshCookie.setMaxAge(7 * 24 * 60 * 60);
response.addCookie(refreshCookie);
```
📌 **File:** `AuthController.java`  
📌 **What Happens?**
- The **refresh token is set in a secure cookie**.
- The user is redirected to `/dashboard`.

---

### **6️⃣ User Requests Data with JWT**
Once logged in, the frontend requests `/api/users/me`.

📌 **Backend Secures Route**
```java
.requestMatchers("/api/users/me").authenticated()
```
📌 **File:** `SecurityConfig.java`

📌 **Example Frontend Request**
```
GET /api/users/me
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

📌 **Backend Verifies JWT**
```java
public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
}
```
📌 **File:** `SecurityConfig.java`

📌 **Find User and Respond**
```java
@GetMapping("/me")
public ResponseEntity<AppUser> getCurrentUser(Authentication authentication) {
    OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
    return ResponseEntity.ok(userService.findByEmail(oidcUser.getEmail()));
}
```
📌 **File:** `AppUserController.java`

📌 **Response**
```json
{
  "id": 1,
  "email": "eatsadvisor@gmail.com",
  "oauthProvider": "google",
  "createdAt": "2025-03-05T08:00:00Z"
}
```
📌 **What Happens?**
- Backend **verifies JWT** and **returns user data**.

---

### **7️⃣ User’s Access Token Expires & Uses Refresh Token**
After expiration, the frontend **requests a new access token** using the refresh token.

📌 **Example Frontend Request**
```
POST /auth/refresh
Cookie: refresh_token=mock_refresh_token_1
```

📌 **Backend Handles Refresh**
```java
@PostMapping("/refresh")
public ResponseEntity<Map<String, String>> refreshAccessToken(@CookieValue("refresh_token") String refreshToken) {
    String newAccessToken = refreshTokenService.refreshAccessToken(refreshToken);
    return ResponseEntity.ok(Map.of("access_token", newAccessToken));
}
```
📌 **File:** `AuthController.java`

📌 **Backend Requests New Access Token**
```java
public String refreshAccessToken(String refreshToken) {
    RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    // Generate new JWT
    return jwtService.generateNewJwt(storedToken.getUserId());
}
```
📌 **File:** `RefreshTokenService.java`

📌 **Response**
```json
{
  "access_token": "new_jwt_token_12345"
}
```
📌 **What Happens?**
- **Validates refresh token** → **Issues new access token**.

---

### **🚀 Done! Full Secure OAuth2 Flow with Refresh Tokens**
Now, **only the backend handles authentication**, ensuring maximum security! ✅ 🚀
## **4️⃣ Backend Extracts JWT from Cookie**
- Every request **automatically includes** the `jwt` cookie.
- **Spring Security extracts it** using `CookieBearerTokenResolver`.
- If the token is valid, the user is **authenticated**.

✅ **No frontend access to the JWT**. ✅ **More secure than LocalStorage.**

---

## **5️⃣ API: Get Current User**
- **The frontend calls `/api/users/me`**.
- **Spring Security authenticates the user from JWT**.
- **Returns user details if authenticated**.

```java
package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserRepository appUserRepository;

    public AppUserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/me")
    public Optional<AppUser> getUserByEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaim("email");
            return appUserRepository.findByEmail(email);
        }
        return Optional.empty();
    }
}
```

---

## **6️⃣ API: Refresh Token (If JWT Expires)**
- **If JWT expires**, the frontend calls `/auth/refresh`.
- **Backend checks the refresh token**.
- **Issues a new JWT and updates the cookie**.

```java
package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    public AuthController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null || !refreshTokenService.validateRefreshToken(refreshToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
            return;
        }

        // Generate a new JWT
        String newJwt = refreshTokenService.generateNewJwt(refreshToken);
        Cookie jwtCookie = new Cookie("jwt", newJwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
    }
}
```

---

## **✅ Conclusion**
✔ **JWT is stored in an HTTP-only cookie** (secure).  
✔ **Refresh token is saved in the database** (never exposed).  
✔ **Spring Security automatically handles JWT authentication**.  
✔ **No frontend access to tokens → More secure**.

🚀 **This is the most secure OAuth2 implementation.** 🚀










Yes, in **Spring Security with OAuth2**, the function to exchange the **Authorization Code** for **ID and Access Tokens** is **implicitly handled** by Spring Security. You do not need to manually implement a method like `exchangeCodeForTokens()` because **Spring Security automatically makes this request to Google**.

---

### **🔹 How Is It Handled Implicitly?**
Spring Security **automates the token exchange** as part of its **OAuth2 login flow**.  
When a user is redirected back to `/login/oauth2/code/google`, **Spring Security does the following automatically:**

1️⃣ **Receives the Authorization Code**
   ```
   GET /login/oauth2/code/google?code=4/0Ad...&state=xyz
   ```

2️⃣ **Automatically makes a POST request to Google's token endpoint:**
   ```
   POST https://oauth2.googleapis.com/token
   Body:
   {
     "client_id": "YOUR_GOOGLE_CLIENT_ID",
     "client_secret": "YOUR_GOOGLE_CLIENT_SECRET",
     "code": "4/0Ad...",
     "grant_type": "authorization_code",
     "redirect_uri": "http://localhost:8080/login/oauth2/code/google"
   }
   ```

3️⃣ **Receives and validates the response:**
   ```json
   {
     "access_token": "ya29.a0AR...",
     "expires_in": 3599,
     "refresh_token": "1//0g...",
     "scope": "openid email profile",
     "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
   ```

4️⃣ **Creates an authenticated session in Spring Security**.
- The **ID Token** is parsed and stored in an **OAuth2AuthenticationToken**.
- The **User's details** (email, name, etc.) are extracted.
- If using a **custom success handler**, you can set cookies or redirect users.

---

### **🔹 Who Calls Google's `/token` Endpoint?**
Spring Security itself **performs the Authorization Code exchange**. This happens **inside Spring Security’s OAuth2 client module**.

📌 **How does Spring Security know what to do?**
Because we **configured the Google provider** in `OAuth2ClientConfig.java`:
```java
@Bean
public ClientRegistrationRepository clientRegistrationRepository(
        @Value("${GOOGLE_OAUTH_CLIENT_ID}") String clientId,
        @Value("${GOOGLE_OAUTH_CLIENT_SECRET}") String clientSecret) {

    return new InMemoryClientRegistrationRepository(
            ClientRegistration.withRegistrationId("google")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // ✅ Uses Authorization Code Flow
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token") // ✅ This is where the exchange happens
                    .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .userNameAttributeName("email")
                    .build()
    );
}
```

Because of **this configuration**, Spring Security automatically:
- Redirects users to Google for authentication.
- Handles the callback (`/login/oauth2/code/google`).
- Exchanges the Authorization Code for tokens.
- Extracts the user information.
- Authenticates the user in **Spring Security Context**.

---

### **🔹 Do We Ever Need to Manually Exchange Tokens?**
In **most cases, no** because Spring Security handles it.

However, if you ever need to **manually refresh an access token**, you might implement something like:

📌 **Example for Refreshing Tokens**
```java
public String refreshAccessToken(String refreshToken) {
    RestTemplate restTemplate = new RestTemplate();
    Map<String, String> body = Map.of(
        "client_id", googleClientId,
        "client_secret", googleClientSecret,
        "refresh_token", refreshToken,
        "grant_type", "refresh_token"
    );

    ResponseEntity<Map> response = restTemplate.postForEntity(
        "https://oauth2.googleapis.com/token", new HttpEntity<>(body), Map.class
    );

    return response.getBody().get("access_token").toString();
}
```
This is **only necessary if you want to refresh tokens manually** instead of relying on Spring Security’s built-in token management.

---

### **🔹 Final Answer**
✔️ You **don’t need** `exchangeCodeForTokens()` because **Spring Security automatically does it**.  
✔️ The token exchange happens **internally** when users hit `/login/oauth2/code/google`.  
✔️ You **only need to manage tokens manually** if you want custom token refresh handling. 🚀