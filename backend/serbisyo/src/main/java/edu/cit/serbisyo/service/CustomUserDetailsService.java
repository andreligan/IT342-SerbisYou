// filepath: c:\Users\John Clyde\Documents\3Y-2S\Systems Integration\Project\IT342-SerbisYou\backend\serbisyo\src\main\java\edu\cit\serbisyo\service\CustomUserDetailsService.java
package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.entity.UserPrincipal;
import edu.cit.serbisyo.repository.UserAuthRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAuthRepository userAuthRepository;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserAuthEntity userAuthEntity = userAuthRepository.findByUserName(userName);
        if (userAuthEntity == null) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user not found");
        }
        
        return new UserPrincipal(userAuthEntity);
    }
}