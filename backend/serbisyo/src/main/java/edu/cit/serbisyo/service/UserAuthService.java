package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.UserAuthRepository;
import edu.cit.serbisyo.util.JwtUtil;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Service
public class UserAuthService {
    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserAuthService(UserAuthRepository userAuthRepository, JwtUtil jwtUtil) {
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
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

    public UserAuthEntity createUserAuth(UserAuthEntity userAuth) {
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        return userAuthRepository.save(userAuth);
    }

    public String registerUser(UserAuthEntity userAuth) {
        if (userAuthRepository.findByEmail(userAuth.getEmail()) != null) {
            return "Email already exists.";
        }
        userAuth.setPassword(passwordEncoder.encode(userAuth.getPassword()));
        userAuthRepository.save(userAuth);
        return "User registered successfully.";
    }

    public String loginUser(UserAuthEntity userAuth) {
        UserAuthEntity existingUser = userAuthRepository.findByEmail(userAuth.getEmail());
        if (existingUser == null || !passwordEncoder.matches(userAuth.getPassword(), existingUser.getPassword())) {
            return "Invalid email or password.";
        }
        return jwtUtil.generateToken(existingUser.getEmail());
    }

}