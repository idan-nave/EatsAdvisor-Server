package com.eatsadvisor.eatsadvisor.config;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenService refreshTokenService;

    public OAuth2LoginSuccessHandler(AppUserRepository appUserRepository, RefreshTokenService refreshTokenService) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String oauthProvider = "google"; // Hardcoded for now

        // Create or retrieve user
        Optional<AppUser> existingUser = appUserRepository.findByEmail(email);
        AppUser user = existingUser.orElseGet(() -> {
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            newUser.setOauthProvider(oauthProvider);
            newUser.setOauthProviderId(oauthUser.getAttribute("sub"));
            return appUserRepository.save(newUser);
        });

        // Generate and store refresh token
        String refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(user.getId()));

        // Set refresh token as an HTTP-only cookie
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // Set to true in production
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        // Redirect to frontend
        response.sendRedirect("/auth/login-success"); // ✅ Redirect to controller
    }
}
