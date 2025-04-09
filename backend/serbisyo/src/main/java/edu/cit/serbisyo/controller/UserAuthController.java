package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.service.UserAuthService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/user-auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    // DELETE user authentication record
    // @DeleteMapping("/{authId}")
    // public ResponseEntity<String> deleteUserAuth(@PathVariable Long authId, @RequestHeader("Authorization") String token) {
    //     // Extract the token (remove "Bearer " prefix)
    //     String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
    
    //     // Validate the token and delete the user
    //     String result = userAuthService.deleteUserAuth(authId, jwt);
    //     return ResponseEntity.ok(result);
    // }

    // CREATE user authentication record
    // @PostMapping
    // public UserAuthEntity createUserAuth(@RequestBody UserAuthEntity userAuth) {
    //     return userAuthService.createUserAuth(userAuth);
    // }

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
}
