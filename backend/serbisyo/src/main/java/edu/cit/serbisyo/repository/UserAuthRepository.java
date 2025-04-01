package edu.cit.serbisyo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.serbisyo.entity.UserAuthEntity;

@Repository
public interface UserAuthRepository extends JpaRepository <UserAuthEntity, Long> {
    
    public UserAuthEntity findByEmail(String email);
    public UserAuthEntity findByUserName(String userName); // Add this method to find by username
}
