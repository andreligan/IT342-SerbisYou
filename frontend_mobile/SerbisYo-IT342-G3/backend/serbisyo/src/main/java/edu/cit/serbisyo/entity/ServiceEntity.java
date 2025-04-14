package edu.cit.serbisyo.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Service")
public class ServiceEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;
    
    @Column(nullable = false)
    private String serviceName;
    
    @Column(length = 500)
    private String serviceDescription;
    
    @Column
    private String priceRange;
    
    @Column
    private String durationEstimate;
    
    @ManyToOne
    @JoinColumn(name = "providerId", nullable = false)
    private ServiceProviderEntity provider;
    
    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private ServiceCategoryEntity category;
    
    // Constructors
    public ServiceEntity() {
    }
    
    public ServiceEntity(String serviceName, String serviceDescription, String priceRange, 
                     String durationEstimate, ServiceProviderEntity provider, ServiceCategoryEntity category) {
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.priceRange = priceRange;
        this.durationEstimate = durationEstimate;
        this.provider = provider;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
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
    
    public String getPriceRange() {
        return priceRange;
    }
    
    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }
    
    public String getDurationEstimate() {
        return durationEstimate;
    }
    
    public void setDurationEstimate(String durationEstimate) {
        this.durationEstimate = durationEstimate;
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
    
    // equals, hashCode, and toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceEntity that = (ServiceEntity) o;
        return Objects.equals(serviceId, that.serviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }
    
    @Override
    public String toString() {
        return "ServiceEntity{" +
                "serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", serviceDescription='" + serviceDescription + '\'' +
                ", priceRange='" + priceRange + '\'' +
                ", durationEstimate='" + durationEstimate + '\'' +
                '}';
    }
} 