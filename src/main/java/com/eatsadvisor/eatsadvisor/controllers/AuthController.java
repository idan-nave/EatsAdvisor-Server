package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    public AuthController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("refresh_token") String refreshToken) {
        String newAccessToken = refreshTokenService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("access_token", newAccessToken));
    }

    @GetMapping("/login-success")
    public void loginSuccess(Authentication authentication, HttpServletResponse response) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser user) {
            String jwt = user.getIdToken().getTokenValue();

            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            response.addCookie(jwtCookie);

            response.sendRedirect("http://localhost:3000/dashboard"); // ✅ Redirect here ONLY
        } else {
            response.sendRedirect("http://localhost:3000/login"); // ✅ Handle failed login
        }
    }


    @GetMapping("/auth/logout")
    public String logout(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                         HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        // Clear JWT & Refresh Token Cookies
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return "Logged out!";
    }

}