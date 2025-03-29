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

    public ServiceProviderEntity registerServiceProvider(ServiceProviderEntity provider) {
        return serviceProviderRepository.save(provider);
    }

    public List<ServiceProviderEntity> getAllServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    public ServiceProviderEntity getServiceProvider(Long providerId) {
        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Service provider not found"));
        return provider;
    }

    public ServiceProviderEntity updateServiceProvider(Long providerId, ServiceProviderEntity updatedProvider) {
        ServiceProviderEntity existingProvider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Service provider not found"));
        existingProvider.setBusinessName(updatedProvider.getBusinessName());
        existingProvider.setYearsOfExperience(updatedProvider.getYearsOfExperience());
        existingProvider.setStatus(updatedProvider.getStatus());
        return serviceProviderRepository.save(existingProvider);
    }

    public String deleteServiceProvider(Long providerId) {
        if (serviceProviderRepository.existsById(providerId)) {
            serviceProviderRepository.deleteById(providerId);
            return "Service provider successfully deleted.";
        }
        return "Service provider not found.";
    }
}