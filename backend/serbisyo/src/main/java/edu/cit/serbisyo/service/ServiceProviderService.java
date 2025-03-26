package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceProviderService {

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    // Create a new service provider
    public ServiceProviderEntity createServiceProvider(ServiceProviderEntity serviceProvider) {
        return serviceProviderRepository.save(serviceProvider);
    }

    // Get all service providers
    public List<ServiceProviderEntity> getAllServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    // Get a service provider by ID
    public Optional<ServiceProviderEntity> getServiceProviderById(int id) {
        return serviceProviderRepository.findById(id);
    }

    // Get service providers by category
    public List<ServiceProviderEntity> getServiceProvidersByCategory(String category) {
        return serviceProviderRepository.findByCategory(category);
    }

    // Get all verified service providers
    public List<ServiceProviderEntity> getVerifiedServiceProviders() {
        return serviceProviderRepository.findByIsVerifiedTrue();
    }

    // Get service providers by user ID
    public List<ServiceProviderEntity> getServiceProvidersByUserId(int userId) {
        return serviceProviderRepository.findByUserId(userId);
    }

    // Update a service provider
    public ServiceProviderEntity updateServiceProvider(int id, ServiceProviderEntity updatedServiceProvider) {
        return serviceProviderRepository.findById(id)
                .map(serviceProvider -> {
                    serviceProvider.setBusinessName(updatedServiceProvider.getBusinessName());
                    serviceProvider.setCategory(updatedServiceProvider.getCategory());
                    serviceProvider.setDescription(updatedServiceProvider.getDescription());
                    serviceProvider.setPhoneNumber(updatedServiceProvider.getPhoneNumber());
                    serviceProvider.setEmail(updatedServiceProvider.getEmail());
                    serviceProvider.setAddress(updatedServiceProvider.getAddress());
                    serviceProvider.setVerified(updatedServiceProvider.isVerified());
                    return serviceProviderRepository.save(serviceProvider);
                })
                .orElse(null);
    }

    // Delete a service provider
    public boolean deleteServiceProvider(int id) {
        if (serviceProviderRepository.existsById(id)) {
            serviceProviderRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
