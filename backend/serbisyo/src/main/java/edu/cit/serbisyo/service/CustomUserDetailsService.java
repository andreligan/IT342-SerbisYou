// filepath: c:\Users\John Clyde\Documents\3Y-2S\Systems Integration\Project\IT342-SerbisYou\backend\serbisyo\src\main\java\edu\cit\serbisyo\service\CustomUserDetailsService.java
package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.UserAuthRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    public CustomUserDetailsService(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuthEntity user = userAuthRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}