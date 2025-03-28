package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/serviceprovider")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    // Create a service provider
    @PostMapping
    public ResponseEntity<ServiceProviderEntity> createServiceProvider(@RequestBody ServiceProviderEntity serviceProvider) {
        return ResponseEntity.ok(serviceProviderService.createServiceProvider(serviceProvider));
    }

    // Get all service providers
    @GetMapping
    public ResponseEntity<List<ServiceProviderEntity>> getAllServiceProviders() {
        return ResponseEntity.ok(serviceProviderService.getAllServiceProviders());
    }

    // Get a service provider by ID
    @GetMapping("/getServiceProvider{providerId}")
    public ResponseEntity<ServiceProviderEntity> getServiceProviderById(@PathVariable int providerId) {
        Optional<ServiceProviderEntity> serviceProvider = serviceProviderService.getServiceProviderById(providerId);
        return serviceProvider.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get service providers by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ServiceProviderEntity>> getServiceProvidersByCategory(@PathVariable String category) {
        return ResponseEntity.ok(serviceProviderService.getServiceProvidersByCategory(category));
    }

    // Get all verified service providers
    @GetMapping("/getAllVerifiedServiceProviders")
    public ResponseEntity<List<ServiceProviderEntity>> getVerifiedServiceProviders() {
        return ResponseEntity.ok(serviceProviderService.getVerifiedServiceProviders());
    }

    // Get service providers by user ID
    //@GetMapping("/user/{userId}")
    //public ResponseEntity<List<ServiceProviderEntity>> getServiceProvidersByUserId(@PathVariable int userId) {
    //    return ResponseEntity.ok(serviceProviderService.getServiceProvidersByUserId(userId));
    //}

    // Update a service provider
    @PutMapping("/updateServiceProvider/{providerId}")
    public ResponseEntity<ServiceProviderEntity> updateServiceProvider(@PathVariable int id, @RequestBody ServiceProviderEntity updatedServiceProvider) {
        ServiceProviderEntity serviceProvider = serviceProviderService.updateServiceProvider(providerId, updatedServiceProvider);
        if (serviceProvider != null) {
            return ResponseEntity.ok(serviceProvider);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a service provider
    @DeleteMapping("/deleteServiceProvider/{providerId}")
    public ResponseEntity<String> deleteServiceProvider(@PathVariable int providerId) {
        boolean deleted = serviceProviderService.deleteServiceProvider(providerId);
        if (deleted) {
            return ResponseEntity.ok("Service provider deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}