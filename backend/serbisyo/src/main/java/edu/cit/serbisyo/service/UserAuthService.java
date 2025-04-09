package edu.cit.serbisyo.service;

import edu.cit.serbisyo.config.JwtUtil;
import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.CustomerRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.UserAuthRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserAuthService {
    private final UserAuthRepository userAuthRepository;
    private final CustomerRepository customerRepository; // Inject CustomerRepository
    private final ServiceProviderRepository serviceProviderRepository; // Inject ServiceProviderRepository
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Constructor injection for dependencies
    public UserAuthService(UserAuthRepository userAuthRepository, CustomerRepository customerRepository, ServiceProviderRepository serviceProviderRepository, JwtUtil jwtUtil) {
        this.userAuthRepository = userAuthRepository;
        this.customerRepository = customerRepository; // Initialize CustomerRepository
        this.serviceProviderRepository = serviceProviderRepository; // Initialize ServiceProviderRepository
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String registerUser(UserAuthEntity userAuth, CustomerEntity customer, ServiceProviderEntity serviceProvider) {
        if (userAuth == null || userAuth.getEmail() == null || userAuth.getPassword() == null) {
            throw new IllegalArgumentException("UserAuthEntity, email, and password cannot be null");
        }
    
        if (userAuthRepository.findByEmail(userAuth.getEmail()) != null) {
            return "Email already exists.";
        }
    
        // Save UserAuthEntity
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        UserAuthEntity savedUserAuth = userAuthRepository.save(userAuth);
    
        // Handle roles
        if ("Customer".equals(userAuth.getRole())) {
            if (customer != null) {
                customer.setUserAuth(savedUserAuth);
                customerRepository.save(customer);
            }
        } else if ("Service Provider".equals(userAuth.getRole())) {
            if (serviceProvider != null) {
                serviceProvider.setUserAuth(savedUserAuth);
                serviceProvider.setVerified(false); // Default value for verified
                serviceProviderRepository.save(serviceProvider);
            }
        } else {
            throw new IllegalArgumentException("Invalid role: " + userAuth.getRole());
        }
    
        return "User registered successfully.";
    }

    // public String deleteUserAuth(Long authId, String token) {
    //     // Validate the token
    //     if (!jwtUtil.validateToken(token)) {
    //         return "Invalid or expired token.";
    //     }

    //     // Optionally, extract the email from the token and check permissions
    //     String email = jwtUtil.extractEmail(token);
    //     UserAuthEntity requestingUser = userAuthRepository.findByEmail(email);
    //     if (requestingUser == null) {
    //         return "Unauthorized request.";
    //     }

    //     // Perform the delete operation
    //     if (userAuthRepository.existsById(authId)) {
    //         userAuthRepository.deleteById(authId);
    //         return "User authentication record successfully deleted.";
    //     }
    //     return "User authentication record not found.";
    // }

// ...existing code...

public Map<String, String> loginUser(UserAuthEntity userAuth) {
    // Check if the user exists by username
    UserAuthEntity existingUser = userAuthRepository.findByUserName(userAuth.getUserName());
    if (existingUser == null || !passwordEncoder.matches(userAuth.getPassword(), existingUser.getPassword())) {
        throw new IllegalArgumentException("Invalid username or password.");
    }

    // Generate the token with username, email and role
    String token = jwtUtil.generateToken(
        existingUser.getUserName(), 
        existingUser.getEmail(), 
        existingUser.getRole()
    );

    // Fetch the user's role
    String role = existingUser.getRole();
    String userId = existingUser.getUserId().toString(); // Assuming you have a getId() method


    // Return both token and role in a Map
    return Map.of("token", token, "role", role, "userId", userId);
}

// ...existing code...

    public Object getAllUserAuth() {
        return userAuthRepository.findAll();
    }

    public String deleteUserAuth(Long authId) {
        if (userAuthRepository.existsById(authId)) {
            userAuthRepository.deleteById(authId);
            return "User authentication record successfully deleted.";
        }
        return "User authentication record not found.";
    }

    public String updateUserAuth(Long authId, UserAuthEntity userAuth) {
        if (userAuthRepository.existsById(authId)) {
            UserAuthEntity existingUserAuth = userAuthRepository.findById(authId).orElse(null);
            if (existingUserAuth != null) {
                existingUserAuth.setUserName(userAuth.getUserName());
                existingUserAuth.setEmail(userAuth.getEmail());
                existingUserAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
                existingUserAuth.setRole(userAuth.getRole());
                userAuthRepository.save(existingUserAuth);
                return "User authentication record successfully updated.";
            }
        }
        return "User authentication record not found.";
    }
}