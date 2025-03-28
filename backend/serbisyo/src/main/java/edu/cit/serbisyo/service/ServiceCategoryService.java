package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.repository.ServiceCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryService() {
        super();
    }

    // CREATE a new service category
    public ServiceCategoryEntity createServiceCategory(ServiceCategoryEntity serviceCategory) {
        return serviceCategoryRepository.save(serviceCategory);
    }

    // READ all service categories
    public List<ServiceCategoryEntity> getAllServiceCategories() {
        return serviceCategoryRepository.findAll();
    }

    // READ a service category by ID
    public ServiceCategoryEntity getServiceCategoryById(Long categoryId) {
        return serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Service Category with ID " + categoryId + " not found"));
    }

    // UPDATE an existing service category
    public ServiceCategoryEntity updateServiceCategory(Long categoryId, ServiceCategoryEntity newCategoryDetails) {
        ServiceCategoryEntity existingCategory = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Service Category with ID " + categoryId + " not found"));

        existingCategory.setCategoryName(newCategoryDetails.getCategoryName());

        return serviceCategoryRepository.save(existingCategory);
    }

    // DELETE a service category
    public String deleteServiceCategory(Long categoryId) {
        if (serviceCategoryRepository.existsById(categoryId)) {
            serviceCategoryRepository.deleteById(categoryId);
            return "Service Category with ID " + categoryId + " has been deleted successfully.";
        } else {
            return "Service Category with ID " + categoryId + " not found.";
        }
    }
}
