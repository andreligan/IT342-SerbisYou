package edu.cit.serbisyo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "UserAuth")
public class UserAuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String userName;
    @Column(nullable = false)
    private String password;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToOne(mappedBy = "userAuth", cascade = CascadeType.ALL)
    private CustomerEntity customer;

    @JsonIgnore
    @OneToOne(mappedBy = "userAuth", cascade = CascadeType.ALL)
    private ServiceProviderEntity serviceProvider;

    @JsonIgnore
    @OneToOne(mappedBy = "userAuth", cascade = CascadeType.ALL)
    private AdminEntity admin;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public ServiceProviderEntity getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProviderEntity serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public AdminEntity getAdmin() {
        return admin;
    }

    public void setAdmin(AdminEntity admin) {
        this.admin = admin;
    }
}
