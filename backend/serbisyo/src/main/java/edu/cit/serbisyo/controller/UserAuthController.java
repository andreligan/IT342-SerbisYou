package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.service.UserAuthService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/user-auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, Object> requestBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserAuthEntity userAuth = mapper.convertValue(requestBody.get("userAuth"), UserAuthEntity.class);
            CustomerEntity customer = mapper.convertValue(requestBody.get("customer"), CustomerEntity.class);
            ServiceProviderEntity serviceProvider = mapper.convertValue(requestBody.get("serviceProvider"), ServiceProviderEntity.class);

            String result = userAuthService.registerUser(userAuth, customer, serviceProvider);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody UserAuthEntity userAuth) {
        try {
            Map<String, String> response = userAuthService.loginUser(userAuth);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Object> getAllUserAuth() {
        return ResponseEntity.ok(userAuthService.getAllUserAuth());
    }
    
    @GetMapping("/getById/{authId}")
    public ResponseEntity<Object> getUserAuthById(@PathVariable Long authId) {
        UserAuthEntity userAuth = userAuthService.getUserAuthById(authId);
        if (userAuth != null) {
            return ResponseEntity.ok(userAuth);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User authentication not found");
        }
    }
    
    @DeleteMapping("/{authId}")
    public ResponseEntity<String> deleteUserAuth(@PathVariable Long authId) {
        String result = userAuthService.deleteUserAuth(authId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update/{authId}")
    public ResponseEntity<String> updateUserAuth(@PathVariable Long authId, @RequestBody UserAuthEntity userAuth) {
        String result = userAuthService.updateUserAuth(authId, userAuth);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(Authentication authentication) {
        // If we get here, the token is already validated by the JWT filter
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("valid", true));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }

    @PutMapping("/change-password/{authId}")
    public ResponseEntity<String> changePassword(
            @PathVariable Long authId,
            @RequestBody Map<String, String> passwordDetails) {
        try {
            String oldPassword = passwordDetails.get("oldPassword");
            String newPassword = passwordDetails.get("newPassword");

            String result = userAuthService.changePassword(authId, oldPassword, newPassword);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
