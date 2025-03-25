package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.repository.ServiceCategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCategoryService {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    // CREATE
    public ServiceCategoryEntity addServiceCategory(ServiceCategoryEntity serviceCategory) {
        return serviceCategoryRepository.save(serviceCategory);
    }

    // READ
    public List<ServiceCategoryEntity> getAllServiceCategories() {
        return serviceCategoryRepository.findAll();
    }

    public Optional<ServiceCategoryEntity> getServiceCategoryById(int id) {
        return serviceCategoryRepository.findById(id);
    }

    // UPDATE
    public ServiceCategoryEntity updateServiceCategory(int id, ServiceCategoryEntity newDetails) {
        return serviceCategoryRepository.findById(id)
            .map(category -> {
                category.setCategoryName(newDetails.getCategoryName());
                return serviceCategoryRepository.save(category);
            }).orElseThrow(() -> new RuntimeException("Service Category not found!"));
    }

    // DELETE
    public String deleteServiceCategory(int id) {
        if (serviceCategoryRepository.existsById(id)) {
            serviceCategoryRepository.deleteById(id);
            return "Service Category successfully deleted!";
        }
        return "Service Category with ID " + id + " not found.";
    }
}