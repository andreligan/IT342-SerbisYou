package edu.cit.serbisyo.service;

import edu.cit.serbisyo.config.JwtUtil;
import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.CustomerRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.UserAuthRepository;
// Import AddressRepository if you need to save address separately
// import edu.cit.serbisyo.repository.AddressRepository; 
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserAuthService {
    private final UserAuthRepository userAuthRepository;
    private final CustomerRepository customerRepository; // Inject CustomerRepository
    private final ServiceProviderRepository serviceProviderRepository; // Inject ServiceProviderRepository
    // Uncomment if you need AddressRepository here
    // private final AddressRepository addressRepository; 
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Constructor injection for dependencies
    // Add AddressRepository to constructor if needed
    public UserAuthService(UserAuthRepository userAuthRepository, CustomerRepository customerRepository, ServiceProviderRepository serviceProviderRepository, /* AddressRepository addressRepository, */ JwtUtil jwtUtil) {
        this.userAuthRepository = userAuthRepository;
        this.customerRepository = customerRepository; // Initialize CustomerRepository
        this.serviceProviderRepository = serviceProviderRepository; // Initialize ServiceProviderRepository
        // this.addressRepository = addressRepository; // Initialize if needed
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String registerUser(UserAuthEntity userAuth, CustomerEntity customer, ServiceProviderEntity serviceProvider) {
        if (userAuth == null || userAuth.getEmail() == null || userAuth.getPassword() == null) {
            throw new IllegalArgumentException("UserAuthEntity, email, and password cannot be null");
        }

        if (userAuthRepository.findByEmail(userAuth.getEmail()) != null) {
            // Consider throwing an exception or returning a specific error response instead of just a string
            throw new IllegalArgumentException("Email already exists."); 
        }

        // Save UserAuthEntity
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        UserAuthEntity savedUserAuth = userAuthRepository.save(userAuth);

        // --- Debugging ---
        String receivedRole = userAuth.getRole(); // Get the role that was saved
        System.out.println("Received role for validation: '" + receivedRole + "'"); 
        // --- End Debugging ---

        // Handle roles - Use equalsIgnoreCase with lowercase strings
        if ("customer".equalsIgnoreCase(receivedRole)) { 
            if (customer != null) {
                customer.setUserAuth(savedUserAuth);
                // Make sure the address is saved *before* the customer if it's a new address
                // Assuming AddressEntity needs to be saved separately if not cascaded
                // AddressEntity savedAddress = addressRepository.save(customer.getAddress()); // Example if needed
                // customer.setAddress(savedAddress); // Example if needed
                customerRepository.save(customer);
                System.out.println("Customer entity processing completed."); // Add log
            } else {
                System.out.println("Role is customer, but customer object is null.");
                // If a customer object is *required* for the 'customer' role, throw an error
                throw new IllegalArgumentException("Customer details are missing for customer role.");
            }
        } else if ("service_provider".equalsIgnoreCase(receivedRole)) { 
            if (serviceProvider != null) {
                serviceProvider.setUserAuth(savedUserAuth);
                serviceProvider.setVerified(false); // Default value for verified
                 // Make sure the address is saved *before* the provider if it's a new address
                 // Assuming AddressEntity needs to be saved separately if not cascaded
                // AddressEntity savedAddress = addressRepository.save(serviceProvider.getAddress()); // Example if needed
                // serviceProvider.setAddress(savedAddress); // Example if needed
                serviceProviderRepository.save(serviceProvider);
                 System.out.println("ServiceProvider entity processing completed."); // Add log
            } else {
                 System.out.println("Role is service_provider, but serviceProvider object is null.");
                 // If a serviceProvider object is *required* for the 'service_provider' role, throw an error
                throw new IllegalArgumentException("ServiceProvider details are missing for service_provider role.");
            }
        } else {
             System.out.println("Role did not match 'customer' or 'service_provider'. Throwing exception."); 
            throw new IllegalArgumentException("Invalid role specified: " + receivedRole);
        }

        return "User registered successfully."; // This might not be reached if an exception occurs
    }

    public Map<String, String> loginUser(UserAuthEntity userAuth) {
        // Check if the user exists by username
        UserAuthEntity existingUser = userAuthRepository.findByUserName(userAuth.getUserName());
        if (existingUser == null || !passwordEncoder.matches(userAuth.getPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
    
        // Generate the token
        String token = jwtUtil.generateToken(existingUser.getUserName());
    
        // Fetch the user's role
        String role = existingUser.getRole();
    
        // Return both token and role in a Map
        return Map.of("token", token, "role", role);
    }

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
}