package edu.cit.serbisyo.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.serbisyo.config.JwtUtil;
import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.UserAuthRepository;
import edu.cit.serbisyo.service.UserAuthService;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {
    
    @Autowired
    private UserAuthService userAuthService;
    
    @Autowired
    private UserAuthRepository userAuthRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerOAuthUser(@RequestBody Map<String, Object> requestBody) {
        try {
            // Extract user data
            Map<String, Object> userAuthMap = (Map<String, Object>) requestBody.get("userAuth");
            String userName = (String) userAuthMap.get("userName");
            String email = (String) userAuthMap.get("email");
            String role = (String) userAuthMap.get("role");
            
            // Check if email already exists
            if (userAuthRepository.findByEmail(email) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already registered"));
            }
            
            // Create user auth entity
            UserAuthEntity userAuth = new UserAuthEntity();
            userAuth.setUserName(userName);
            userAuth.setEmail(email);
            // Generate random password for OAuth users
            String randomPassword = UUID.randomUUID().toString();
            userAuth.setPassword(randomPassword);  // UserAuthService will encode it
            userAuth.setRole(role);
            
            // Create profile based on role
            CustomerEntity customer = null;
            ServiceProviderEntity serviceProvider = null;
            
            if ("Customer".equals(role)) {
                Map<String, Object> customerMap = (Map<String, Object>) requestBody.get("customer");
                customer = new CustomerEntity();
                customer.setFirstName((String) customerMap.get("firstName"));
                customer.setLastName((String) customerMap.get("lastName"));
                customer.setPhoneNumber((String) customerMap.get("phoneNumber"));
                
                // Handle address
                Map<String, Object> addressMap = (Map<String, Object>) requestBody.get("address");
                AddressEntity address = new AddressEntity();
                address.setStreetName((String) addressMap.get("streetName"));
                address.setBarangay((String) addressMap.get("barangay"));
                address.setCity((String) addressMap.get("city"));
                address.setProvince((String) addressMap.get("province"));
                address.setZipCode((String) addressMap.get("zipCode"));
                
                customer.setAddress(List.of(address));
            } else if ("Service Provider".equals(role)) {
                Map<String, Object> providerMap = (Map<String, Object>) requestBody.get("serviceProvider");
                serviceProvider = new ServiceProviderEntity();
                serviceProvider.setFirstName((String) providerMap.get("firstName"));
                serviceProvider.setLastName((String) providerMap.get("lastName"));
                serviceProvider.setPhoneNumber((String) providerMap.get("phoneNumber"));
                serviceProvider.setBusinessName((String) providerMap.get("businessName"));
                serviceProvider.setVerified(false);
                
                // Handle address
                Map<String, Object> addressMap = (Map<String, Object>) requestBody.get("address");
                AddressEntity address = new AddressEntity();
                address.setStreetName((String) addressMap.get("streetName"));
                address.setBarangay((String) addressMap.get("barangay"));
                address.setCity((String) addressMap.get("city"));
                address.setProvince((String) addressMap.get("province"));
                address.setZipCode((String) addressMap.get("zipCode"));
                
                serviceProvider.setAddresses(List.of(address));
            }
            
            // Register user
            String result = userAuthService.registerUser(userAuth, customer, serviceProvider);
            
            // On success, generate token and return user details
            UserAuthEntity createdUser = userAuthRepository.findByEmail(email);
            String token = jwtUtil.generateToken(createdUser.getUserName(), createdUser.getEmail(), createdUser.getRole());
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", createdUser.getUserId().toString(),
                "role", createdUser.getRole(),
                "message", "Registration successful"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }
}