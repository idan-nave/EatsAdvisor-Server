package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        jwtCookie.setSecure(false); // We're not using HTTPS in development
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        jwtCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(jwtCookie);
        
        // Add a non-HttpOnly cookie for frontend detection
        Cookie jwtIndicatorCookie = new Cookie("jwt_present", "true");
        jwtIndicatorCookie.setHttpOnly(false);
        jwtIndicatorCookie.setSecure(false); // We're not using HTTPS in development
        jwtIndicatorCookie.setPath("/");
        jwtIndicatorCookie.setMaxAge(3600);
        jwtIndicatorCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(jwtIndicatorCookie);
    }

    @GetMapping("/login-success")
    public void loginSuccess(Authentication authentication, HttpServletResponse response) throws IOException {
        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser user)) {
            System.out.println("❌ Login failed: Authentication is null or invalid");
            response.sendRedirect("http://localhost:3001/login");
            return;
        }

        // Debugging log
        System.out.println("✅ Login successful! User: " + user.getEmail());

        // Get JWT from the OIDC user
        String jwt = user.getIdToken().getTokenValue();
        System.out.println("Generated JWT: " + jwt);

        // Set JWT as a cookie
        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // We're not using HTTPS in development
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        jwtCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(jwtCookie);
        
        // Add a non-HttpOnly cookie for frontend detection
        Cookie jwtIndicatorCookie = new Cookie("jwt_present", "true");
        jwtIndicatorCookie.setHttpOnly(false);
        jwtIndicatorCookie.setSecure(false); // We're not using HTTPS in development
        jwtIndicatorCookie.setPath("/");
        jwtIndicatorCookie.setMaxAge(3600);
        jwtIndicatorCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(jwtIndicatorCookie);

        System.out.println("✅ JWT Cookie set successfully!");

        response.sendRedirect("http://localhost:3001/dashboard");
    }

    @GetMapping("/logout")
    public String logout(@CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        // Clear JWT & Refresh Token Cookies
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // We're not using HTTPS in development
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(jwtCookie);
        
        // Clear the JWT indicator cookie
        Cookie jwtIndicatorCookie = new Cookie("jwt_present", "");
        jwtIndicatorCookie.setHttpOnly(false);
        jwtIndicatorCookie.setSecure(false); // We're not using HTTPS in development
        jwtIndicatorCookie.setPath("/");
        jwtIndicatorCookie.setMaxAge(0);
        jwtIndicatorCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(jwtIndicatorCookie);

        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // We're not using HTTPS in development
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        refreshCookie.setAttribute("SameSite", "Lax"); // Changed from None to Lax for non-HTTPS
        response.addCookie(refreshCookie);

        return "Logged out!";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("🔍 Checking authentication: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            System.out.println("❌ User is not authenticated");
            return ResponseEntity.status(401).body("Not authenticated");
        }

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            System.out.println("✅ Authenticated user: " + oidcUser.getEmail());

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("email", oidcUser.getEmail());
            userInfo.put("name", oidcUser.getFullName());
            return ResponseEntity.ok(userInfo);
        }

        return ResponseEntity.status(401).body("Invalid authentication");
    }

}
