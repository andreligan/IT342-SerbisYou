package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.service.UserAuthService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/user-auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    // DELETE user authentication record
    @DeleteMapping("/{authId}")
    public ResponseEntity<String> deleteUserAuth(@PathVariable Long authId, @RequestHeader("Authorization") String token) {
        // Extract the token (remove "Bearer " prefix)
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
    
        // Validate the token and delete the user
        String result = userAuthService.deleteUserAuth(authId, jwt);
        return ResponseEntity.ok(result);
    }

    // CREATE user authentication record
    @PostMapping
    public UserAuthEntity createUserAuth(@RequestBody UserAuthEntity userAuth) {
        return userAuthService.createUserAuth(userAuth);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserAuthEntity userAuth) {
        String result = userAuthService.registerUser(userAuth);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody UserAuthEntity userAuth) {
        String token = userAuthService.loginUser(userAuth);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
