package edu.cit.serbisyo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.repository.ServiceCategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCategoryService {
    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryService(ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    public List<ServiceCategoryEntity> getAllCategories() {
        return serviceCategoryRepository.findAll();
    }

    public Optional<ServiceCategoryEntity> getCategoryById(Long id) {
        return serviceCategoryRepository.findById(id);
    }

    @Transactional
    public ServiceCategoryEntity createCategory(ServiceCategoryEntity category) {
        return serviceCategoryRepository.save(category);
    }

    @Transactional
    public ServiceCategoryEntity updateCategory(Long id, ServiceCategoryEntity category) {
        Optional<ServiceCategoryEntity> existingCategory = serviceCategoryRepository.findById(id);
        
        if (existingCategory.isPresent()) {
            ServiceCategoryEntity categoryToUpdate = existingCategory.get();
            categoryToUpdate.setCategoryName(category.getCategoryName());
            categoryToUpdate.setDescription(category.getDescription());
            
            return serviceCategoryRepository.save(categoryToUpdate);
        }
        
        return null;
    }

    public boolean deleteCategory(Long id) {
        if (serviceCategoryRepository.existsById(id)) {
            serviceCategoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 