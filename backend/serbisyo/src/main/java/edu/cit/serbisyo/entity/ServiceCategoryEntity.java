package edu.cit.serbisyo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class ServiceCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int serviceCategoryId;

    private String categoryName;

    public ServiceCategoryEntity() {
        super();
    }

    public ServiceCategoryEntity(int serviceCategoryId, String categoryName) {
        this.serviceCategoryId = serviceCategoryId;
        this.categoryName = categoryName;
    }

    public int getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(int serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}