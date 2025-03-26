package edu.cit.serbisyo.service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.UserAuthRepository;

@Service
public class UserAuthService {

    @Autowired
    UserAuthRepository urepo;

    public UserAuthService() {
        super();
    }

    // CREATE
    public UserAuthEntity postUserAuth(UserAuthEntity userauth) {
        return urepo.save(userauth);
        
    }

    // READ
    public List<UserAuthEntity> getAllUserAuth() {
        return urepo.findAll();
    }

    // UPDATE
    @SuppressWarnings("finally")
    public UserAuthEntity putUserAuthDetails(int userId, UserAuthEntity newUserAuthDetails) {
        UserAuthEntity userauth = new UserAuthEntity();

        try {
            userauth = urepo.findById(userId).get();

            userauth.setEmail(newUserAuthDetails.getEmail());
            userauth.setUsername(newUserAuthDetails.getUsername());
            userauth.setPassword(newUserAuthDetails.getPassword());
            userauth.setRole(newUserAuthDetails.getRole());
            userauth.setCreatedAt(newUserAuthDetails.getCreatedAt());


        } catch(NoSuchElementException nex) {
            throw new NameNotFoundException ("user Authentication " + userId + " not found.");
        } finally {
            return urepo.save(userauth);
        }

    }

    // DELETE
    public String deleteUserAuthDetails(int userId) {
        String msg = "";
        if(urepo.findById(userId).isPresent()) {
            urepo.deleteById(userId);
            msg = "School Record sucessfully deleted.";
        } else {
            return "User Authentication ID " + userId + " not found.";
        }
        return msg;
    }
    
}
