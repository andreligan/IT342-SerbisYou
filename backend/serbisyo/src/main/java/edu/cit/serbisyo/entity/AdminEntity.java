package edu.cit.serbisyo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Admin")
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    private UserAuthEntity userAuth;

    // Getters and Setters

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public UserAuthEntity getUser() {
        return userAuth;
    }

    public void setUser(UserAuthEntity user) {
        this.userAuth = user;
    }
}
