package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.repository.ServiceRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.ServiceCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    public ServiceService() {
        super();
    }

    // CREATE a new service
    public ServiceEntity createService(Long providerId, Long categoryId, ServiceEntity serviceDetails) {
        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new NoSuchElementException("Service Provider with ID " + providerId + " not found"));

        ServiceCategoryEntity category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Service Category with ID " + categoryId + " not found"));

        serviceDetails.setProvider(provider);
        serviceDetails.setCategory(category);

        return serviceRepository.save(serviceDetails);
    }

    // READ all services
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    // READ a service by ID
    public ServiceEntity getServiceById(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NoSuchElementException("Service with ID " + serviceId + " not found"));
    }

    // UPDATE an existing service
    public ServiceEntity updateService(Long serviceId, Long providerId, Long categoryId, ServiceEntity newServiceDetails) {
        ServiceEntity existingService = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NoSuchElementException("Service with ID " + serviceId + " not found"));

        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new NoSuchElementException("Service Provider with ID " + providerId + " not found"));

        ServiceCategoryEntity category = serviceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Service Category with ID " + categoryId + " not found"));

        existingService.setProvider(provider);
        existingService.setCategory(category);
        existingService.setServiceName(newServiceDetails.getServiceName());
        existingService.setServiceDescription(newServiceDetails.getServiceDescription());
        existingService.setPrice(newServiceDetails.getPrice()); // Updated from priceRange to price
        existingService.setDurationEstimate(newServiceDetails.getDurationEstimate());

        return serviceRepository.save(existingService);
    }

    // DELETE a service
    public String deleteService(Long serviceId) {
        if (serviceRepository.existsById(serviceId)) {
            serviceRepository.deleteById(serviceId);
            return "Service with ID " + serviceId + " has been deleted successfully.";
        } else {
            return "Service with ID " + serviceId + " not found.";
        }
    }

    public String uploadServiceImage(Long serviceId, MultipartFile image) throws IOException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        // Ensure the uploads directory exists
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir); // Create the directory if it doesn't exist
        }

        // Save the image to a local directory
        String fileName = serviceId + "_" + image.getOriginalFilename();
        Path filePath = Paths.get("uploads/" + fileName);
        Files.write(filePath, image.getBytes());

        // Save the file path to the database
        service.setServiceImage(filePath.toString());
        serviceRepository.save(service);

        return "Service image uploaded successfully.";
    }

    public String getServiceImage(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (service.getServiceImage() == null || service.getServiceImage().isEmpty()) {
            throw new IllegalArgumentException("Service image not found for the service.");
        }

        System.out.println("Service Image Path: " + "/uploads/" + Paths.get(service.getServiceImage()).getFileName().toString());

        // Return the relative path of the service image
        return "/uploads/" + Paths.get(service.getServiceImage()).getFileName().toString();
    }
}
