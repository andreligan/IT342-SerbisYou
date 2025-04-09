package edu.cit.serbisyo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.ServiceCategoryRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.ServiceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceService(
            ServiceRepository serviceRepository,
            ServiceProviderRepository serviceProviderRepository,
            ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    public Optional<ServiceEntity> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    @Transactional
    public ServiceEntity createService(
            Long providerId,
            Long categoryId,
            String serviceName,
            String serviceDescription,
            String priceRange,
            String durationEstimate) {
        
        // Find the service provider
        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Service Provider not found with ID: " + providerId));
        
        // Find the service category
        ServiceCategoryEntity category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Service Category not found with ID: " + categoryId));
        
        // Create and save the new service
        ServiceEntity newService = new ServiceEntity(
                serviceName,
                serviceDescription,
                priceRange,
                durationEstimate,
                provider,
                category
        );
        
        return serviceRepository.save(newService);
    }

    @Transactional
    public ServiceEntity updateService(
            Long serviceId,
            Long providerId,
            Long categoryId,
            String serviceName,
            String serviceDescription,
            String priceRange,
            String durationEstimate) {
        
        // Check if service exists
        ServiceEntity existingService = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));
        
        // Find the service provider
        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Service Provider not found with ID: " + providerId));
        
        // Find the service category
        ServiceCategoryEntity category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Service Category not found with ID: " + categoryId));
        
        // Update the service properties
        existingService.setServiceName(serviceName);
        existingService.setServiceDescription(serviceDescription);
        existingService.setPriceRange(priceRange);
        existingService.setDurationEstimate(durationEstimate);
        existingService.setProvider(provider);
        existingService.setCategory(category);
        
        // Save and return the updated service
        return serviceRepository.save(existingService);
    }

    public boolean deleteService(Long serviceId) {
        if (serviceRepository.existsById(serviceId)) {
            serviceRepository.deleteById(serviceId);
            return true;
        }
        return false;
    }

    public void deleteAllServices() {
        serviceRepository.deleteAll();
    }
} 