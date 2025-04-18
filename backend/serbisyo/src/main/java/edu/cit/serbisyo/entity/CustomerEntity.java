package edu.cit.serbisyo.entity;

import jakarta.persistence.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Customer")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    private UserAuthEntity userAuth;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<AddressEntity> address;

    private String firstName;
    private String lastName;
    private String phoneNumber;

    @Lob
    private String profileImage;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<BookingEntity> bookings;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<ReviewEntity> reviews;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public UserAuthEntity getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(UserAuthEntity userAuth) {
        this.userAuth = userAuth;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<AddressEntity> getAddress() {
        return address;
    }

    public void setAddress(List<AddressEntity> address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
