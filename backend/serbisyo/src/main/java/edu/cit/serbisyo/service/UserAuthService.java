package edu.cit.serbisyo.service;

import edu.cit.serbisyo.repository.UserAuthRepository;
import org.springframework.stereotype.Service;


@Service
public class UserAuthService {
    private final UserAuthRepository userAuthRepository;

    public UserAuthService(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    public String deleteUserAuth(Long authId) {
        if (userAuthRepository.existsById(authId)) {
            userAuthRepository.deleteById(authId);
            return "User authentication record successfully deleted.";
        }
        return "User authentication record not found.";
    }
}