package edu.cit.serbisyo.security;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import edu.cit.serbisyo.config.JwtUtil;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.UserAuthRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserAuthRepository userAuthRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        // Get OAuth2 user information
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extract email and name from Google user info
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        
        // Check if user exists, or create a new one
        Optional<UserAuthEntity> existingUser = Optional.ofNullable(userAuthRepository.findByEmail(email));
        UserAuthEntity user = existingUser.orElseGet(() -> {
            // Create a new user with Google OAuth info
            UserAuthEntity newUser = new UserAuthEntity();
            newUser.setEmail(email);
            newUser.setUserName(email); // Use email as username initially
            // Generate a random password since OAuth2 users don't need it
            newUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(java.util.UUID.randomUUID().toString()));
            newUser.setRole("Customer"); // Default role
            return userAuthRepository.save(newUser);
        });
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUserName(), user.getEmail(), user.getRole());
        
        // Build redirect URL with token parameters
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("userId", user.getUserId())
                .queryParam("role", user.getRole())
                .build().toUriString();
        
        // Set status to OK
        response.setStatus(HttpServletResponse.SC_OK);
        
        // Redirect to frontend with token
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}