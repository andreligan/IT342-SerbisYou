package edu.cit.serbisyo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ServiceProvider")
public class ServiceProviderEntity {
    @Id
    private Long providerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "userId")
    private UserAuthEntity userAuth;

    @ManyToOne
    @JoinColumn(name = "addressId", nullable = false)
    private AddressEntity address;

    @OneToOne(mappedBy = "serviceProvider")
    private VerificationEntity verification;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String businessName;
    private int yearsOfExperience;
    private String availabilitySchedule;
    private double averageRating;
    private String status;
    private String paymentMethod;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    private List<ServiceEntity> services;


    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public UserAuthEntity getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(UserAuthEntity userAuth) {
        this.userAuth = userAuth;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getAvailabilitySchedule() {
        return availabilitySchedule;
    }

    public void setAvailabilitySchedule(String availabilitySchedule) {
        this.availabilitySchedule = availabilitySchedule;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<ServiceEntity> getServices() {
        return services;
    }

    public void setServices(List<ServiceEntity> services) {
        this.services = services;
    }

    public VerificationEntity getVerification() {
        return verification;
    }

    public void setVerification(VerificationEntity verification) {
        this.verification = verification;
    }
}
