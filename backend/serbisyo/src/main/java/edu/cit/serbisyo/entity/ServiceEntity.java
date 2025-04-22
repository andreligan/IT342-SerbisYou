package edu.cit.serbisyo.entity;

import jakarta.persistence.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "providerId", nullable = false)
    private ServiceProviderEntity provider;

    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private ServiceCategoryEntity category;

    private String serviceName;
    private String serviceDescription;
    private int price; // Changed from String priceRange
    private String durationEstimate;
    
    @Lob
    private String serviceImage;

    @JsonIgnore
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<BookingEntity> bookings;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public ServiceProviderEntity getProvider() {
        return provider;
    }

    public void setProvider(ServiceProviderEntity provider) {
        this.provider = provider;
    }

    public ServiceCategoryEntity getCategory() {
        return category;
    }

    public void setCategory(ServiceCategoryEntity category) {
        this.category = category;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    // Modified getter and setter for price
    public int getPrice() {
        return price;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }

    public String getDurationEstimate() {
        return durationEstimate;
    }

    public void setDurationEstimate(String durationEstimate) {
        this.durationEstimate = durationEstimate;
    }

    public List<BookingEntity> getBookings() {
        return bookings;
    }

    public void setBookings(List<BookingEntity> bookings) {
        this.bookings = bookings;
    }

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }
}
