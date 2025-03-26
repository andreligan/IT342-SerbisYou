package edu.cit.serbisyo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserAuthEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int userId;

    private String email;
    
    private String userName;

    private String password;

    private String role;

    private String createdAt;

    public UserAuthEntity() {
        super();
    }

    public UserAuthEntity(int userId, String email, String userName, String password, String role, String createdAt) {
       this.userId = userId;
       this.email = email;
       this.userName = userName;
       this.password = password;
       this.role = role;   
       this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
    

