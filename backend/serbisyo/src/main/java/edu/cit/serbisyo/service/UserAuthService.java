package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.CustomerRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.UserAuthRepository;
import edu.cit.serbisyo.util.JwtUtil;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
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
        if (userAuthRepository.findByEmail(userAuth.getEmail()) != null) {
            return "Email already exists.";
        }

        // Step 1: Save UserAuthEntity
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        UserAuthEntity savedUserAuth = userAuthRepository.save(userAuth);

        // Step 2: Save CustomerEntity if the role is "Customer"
        if ("Customer".equals(userAuth.getRole()) && customer != null) {
            customer.setUserAuth(savedUserAuth); // Associate the saved UserAuthEntity
            customerRepository.save(customer); // Save the CustomerEntity
        }

        // Step 3: Save ServiceProviderEntity if the role is "Service Provider"
        if ("Service Provider".equals(userAuth.getRole()) && serviceProvider != null) {
            serviceProvider.setUserAuth(savedUserAuth); // Associate the saved UserAuthEntity
            serviceProvider.setVerified(false); // Default value for verified
            serviceProviderRepository.save(serviceProvider); // Save the ServiceProviderEntity
        }

        return "User registered successfully.";
    }

    public String deleteUserAuth(Long authId, String token) {
        // Validate the token
        if (!jwtUtil.validateToken(token)) {
            return "Invalid or expired token.";
        }

        // Optionally, extract the email from the token and check permissions
        String email = jwtUtil.extractEmail(token);
        UserAuthEntity requestingUser = userAuthRepository.findByEmail(email);
        if (requestingUser == null) {
            return "Unauthorized request.";
        }

        // Perform the delete operation
        if (userAuthRepository.existsById(authId)) {
            userAuthRepository.deleteById(authId);
            return "User authentication record successfully deleted.";
        }
        return "User authentication record not found.";
    }

    public String loginUser(UserAuthEntity userAuth) {
        UserAuthEntity existingUser = userAuthRepository.findByEmail(userAuth.getEmail());
        if (existingUser == null || !passwordEncoder.matches(userAuth.getPassword(), existingUser.getPassword())) {
            return "Invalid email or password.";
        }
        return jwtUtil.generateToken(existingUser.getEmail());
    }
}