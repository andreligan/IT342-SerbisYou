package edu.cit.serbisyo.entity;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ServiceProvider")
public class ServiceProviderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;

    @OneToOne
    @JoinColumn(name = "userId")
    private UserAuthEntity userAuth;

    @JsonIgnore
    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL)
    private List<AddressEntity> addresses;

    @OneToOne(mappedBy = "serviceProvider")
    private VerificationEntity verification;

    private boolean verified;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String businessName;
    private int yearsOfExperience;
    private String availabilitySchedule;
    private double averageRating;
    private String status;
    private String paymentMethod;

    @Lob
    private String serviceProviderImage;

    @JsonIgnore
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    private List<ServiceEntity> services;

    @JsonIgnore
    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

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

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressEntity> addresses) {
        this.addresses = addresses;
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

    public String getServiceProviderImage() {
        return serviceProviderImage;
    }

    public void setServiceProviderImage(String serviceProviderImage) {
        this.serviceProviderImage = serviceProviderImage;
    }

    public List<ScheduleEntity> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleEntity> schedules) {
        this.schedules = schedules;
    }
}
