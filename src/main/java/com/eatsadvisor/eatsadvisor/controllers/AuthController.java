package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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

    @GetMapping("/login-success")
    public void loginSuccess(Authentication authentication, HttpServletResponse response) throws IOException {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser user) {
            String jwt = user.getIdToken().getTokenValue();

            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            response.addCookie(jwtCookie);

            response.sendRedirect("http://localhost:3001/dashboard"); // Redirect to frontend
        } else {
            response.sendRedirect("http://localhost:3001/login"); // Handle failed login
        }
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
