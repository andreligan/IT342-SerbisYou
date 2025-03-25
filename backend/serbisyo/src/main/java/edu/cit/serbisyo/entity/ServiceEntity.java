package edu.cit.serbisyo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int serviceId;

    private String serviceName;
    private String description;
    private String priceRange;
    private String durationEstimate;

    public ServiceEntity() {
        super();
    }

    public ServiceEntity(int serviceId, String serviceName, String description, String priceRange, String durationEstimate) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.priceRange = priceRange;
        this.durationEstimate = durationEstimate;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}