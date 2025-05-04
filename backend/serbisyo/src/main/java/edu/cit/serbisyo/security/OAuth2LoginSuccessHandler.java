package edu.cit.serbisyo.security;

import java.io.IOException;
import java.util.Map;

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
    
    @Value("${app.frontend.url:https://serbisyo.vercel.app}")
    private String frontendUrl;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserAuthRepository userAuthRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        // Set CORS headers to allow redirect to frontend
        response.setHeader("Access-Control-Allow-Origin", frontendUrl);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,Accept,Origin");
        
        // Get OAuth2 user information
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extract email and name from Google user info
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        
        // Check if user exists
        UserAuthEntity existingUser = userAuthRepository.findByEmail(email);
        
        // Check if request is from a mobile device
        String platform = request.getParameter("platform");
        boolean isMobile = "android".equalsIgnoreCase(platform) || 
                          "true".equalsIgnoreCase(request.getParameter("mobile"));
        
        // Get custom redirect URI if provided (for mobile apps)
        String redirectUri = request.getParameter("redirect_uri");
        
        String redirectUrl;
        
        // If we have mobile parameters, use them for redirection
        if (isMobile && redirectUri != null && !redirectUri.isEmpty()) {
            // Use the mobile app's custom URI scheme
            if (existingUser == null) {
                // For new users, redirect with user info
                redirectUrl = UriComponentsBuilder
                    .fromUriString(redirectUri)
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .queryParam("picture", picture)
                    .build().toUriString();
            } else {
                // For existing users, generate token
                String token = jwtUtil.generateToken(existingUser.getUserName(), existingUser.getEmail(), existingUser.getRole());
                
                redirectUrl = UriComponentsBuilder
                    .fromUriString(redirectUri)
                    .queryParam("token", token)
                    .queryParam("userId", existingUser.getUserId())
                    .queryParam("role", existingUser.getRole())
                    .build().toUriString();
            }
        } else {
            // Determine the correct frontend URL based on the origin
            String origin = request.getHeader("Origin");
            String redirectBaseUrl = (origin != null && origin.contains("vercel.app")) 
                ? "https://serbisyo.vercel.app" 
                : frontendUrl;
                
            if (existingUser == null) {
                // For new users, redirect to role selection
                redirectUrl = UriComponentsBuilder
                    .fromUriString(redirectBaseUrl + "/oauth-role-selection")
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .queryParam("picture", picture)
                    .build().toUriString();
            } else {
                // For existing users, generate token and redirect to home
                String token = jwtUtil.generateToken(existingUser.getUserName(), existingUser.getEmail(), existingUser.getRole());
                
                redirectUrl = UriComponentsBuilder
                    .fromUriString(redirectBaseUrl + "/oauth2/redirect")
                    .queryParam("token", token)
                    .queryParam("userId", existingUser.getUserId())
                    .queryParam("role", existingUser.getRole())
                    .build().toUriString();
            }
        }
        
        // Redirect to frontend
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}