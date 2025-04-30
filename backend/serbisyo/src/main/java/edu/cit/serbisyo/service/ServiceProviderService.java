package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    public ServiceProviderEntity getServiceProviderByAuthId(Long authId) {
        // Use the repository method to find by userAuth.userId
        return serviceProviderRepository.findByUserAuthUserId(authId).orElse(null);
    }

    public ServiceProviderEntity updateServiceProvider(Long providerId, ServiceProviderEntity updatedProvider) {
        ServiceProviderEntity existingProvider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Service provider not found"));
        existingProvider.setFirstName(updatedProvider.getFirstName());
        existingProvider.setLastName(updatedProvider.getLastName());
        existingProvider.setPhoneNumber(updatedProvider.getPhoneNumber());
        existingProvider.setBusinessName(updatedProvider.getBusinessName());
        existingProvider.setYearsOfExperience(updatedProvider.getYearsOfExperience());
        existingProvider.setStatus(updatedProvider.getStatus());
        existingProvider.setPaymentMethod(updatedProvider.getPaymentMethod());
        return serviceProviderRepository.save(existingProvider);
    }

    public String deleteServiceProvider(Long providerId) {
        if (serviceProviderRepository.existsById(providerId)) {
            serviceProviderRepository.deleteById(providerId);
            return "Service provider successfully deleted.";
        }
        return "Service provider not found.";
    }

    public String uploadServiceProviderImage(Long providerId, MultipartFile image) throws IOException {
        ServiceProviderEntity serviceProvider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Service Provider not found"));

        // Ensure the uploads directory exists
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir); // Create the directory if it doesn't exist
        }
        
        // Save the image to a local directory or cloud storage
        String fileName = providerId + "_" + image.getOriginalFilename();
        Path filePath = Paths.get("uploads/" + fileName);
        Files.write(filePath, image.getBytes());

        // Save the file path to the database
        serviceProvider.setServiceProviderImage(filePath.toString());
        serviceProviderRepository.save(serviceProvider);

        return "Profile image uploaded successfully.";
    }

    public String getServiceProviderImage(Long providerId) {
        ServiceProviderEntity serviceProvider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    
        if (serviceProvider.getServiceProviderImage() == null || serviceProvider.getServiceProviderImage().isEmpty()) {
            throw new IllegalArgumentException("Profile image not found for the service provider.");
        }
    
        // Return the relative path of the profile image
        return "/uploads/" + Paths.get(serviceProvider.getServiceProviderImage()).getFileName().toString();
    }
}