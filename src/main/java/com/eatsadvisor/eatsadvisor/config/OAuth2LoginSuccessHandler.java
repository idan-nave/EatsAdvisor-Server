package com.eatsadvisor.eatsadvisor.config;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import com.eatsadvisor.eatsadvisor.services.RefreshTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.backend-base-url}")    private String backendBaseUrl;
    @Value("${app.frontend-base-url}")    private String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(AppUserRepository appUserRepository, ProfileRepository profileRepository,
            RefreshTokenService refreshTokenService) {
        this.appUserRepository = appUserRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {


        System.out.println("✅ OAuth2LoginSuccessHandler: Authentication successful");

        if (!(authentication.getPrincipal() instanceof OAuth2User)) {
            System.out.println("❌ OAuth2LoginSuccessHandler: Principal is not an OAuth2User");
            response.sendRedirect(frontendBaseUrl+"/login?error=invalid_authentication");
            return;
        }

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String oauthProvider = "google"; // Hardcoded for now

        System.out.println("✅ OAuth2LoginSuccessHandler: User email: " + email);

        // Extract user info from OAuth2 user details
        Map<String, Object> attributes = oauthUser.getAttributes();
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        // Create or retrieve user
        Optional<AppUser> existingUser = appUserRepository.findByEmail(email);
        AppUser user = existingUser.orElseGet(() -> {
            System.out.println("✅ OAuth2LoginSuccessHandler: Creating new user");
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setOauthProvider(oauthProvider);
            newUser.setOauthProviderId(oauthUser.getAttribute("sub"));
            AppUser savedUser = appUserRepository.save(newUser);

            return savedUser;
        });

        // Generate and store refresh token
        String refreshToken = refreshTokenService.createRefreshToken(Long.valueOf(user.getId()));
        System.out.println("✅ OAuth2LoginSuccessHandler: Generated refresh token");

        // Get JWT from the OIDC user (if available)
        String jwt = null;
        if (oauthUser.getAttributes().containsKey("id_token")) {
            jwt = (String) oauthUser.getAttributes().get("id_token");
        } else if (authentication instanceof OAuth2AuthenticationToken) {
            // Try to get the ID token from the authentication
            OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
            if (oauth2Auth.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) oauth2Auth.getPrincipal();
                jwt = oidcUser.getIdToken().getTokenValue();
            }
        }

        if (jwt == null) {
            // If no JWT is available, generate one using the refresh token
            System.out.println("⚠️ OAuth2LoginSuccessHandler: No JWT found, generating one");
            jwt = refreshTokenService.generateNewJwt(refreshToken);
        }

        System.out.println("✅ OAuth2LoginSuccessHandler: JWT length: " + jwt.length());

        // Set JWT as a cookie
        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600); // 1 hour
        jwtCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtCookie);
        System.out.println("✅ OAuth2LoginSuccessHandler: Set JWT cookie");

        // Add a non-HttpOnly cookie for frontend detection
        Cookie jwtIndicatorCookie = new Cookie("jwt_present", "true");
        jwtIndicatorCookie.setHttpOnly(false);
        jwtIndicatorCookie.setSecure(true);
        jwtIndicatorCookie.setPath("/");
        jwtIndicatorCookie.setMaxAge(3600); // 1 hour
        jwtIndicatorCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtIndicatorCookie);
        System.out.println("✅ OAuth2LoginSuccessHandler: Set jwt_present cookie");

        // Set refresh token as an HTTP-only cookie
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        refreshCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshCookie);
        System.out.println("✅ OAuth2LoginSuccessHandler: Set refresh_token cookie");

        // Redirect directly to frontend
        System.out.println("✅ OAuth2LoginSuccessHandler: Redirecting to frontend");
        response.sendRedirect(frontendBaseUrl);
    }
}
