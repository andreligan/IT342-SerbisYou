package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.service.ServiceProviderService;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    @PostMapping("/register")
    public ServiceProviderEntity registerServiceProvider(@RequestBody ServiceProviderEntity provider) {
        return serviceProviderService.registerServiceProvider(provider);
    }

    @GetMapping("/getAll")
    public List<ServiceProviderEntity> getAllServiceProviders() {
        return serviceProviderService.getAllServiceProviders();
    }

    @GetMapping("/getById/{providerId}")
    public ServiceProviderEntity getServiceProvider(@PathVariable Long providerId) {
        return serviceProviderService.getServiceProvider(providerId);
    }

    @GetMapping("/getByAuthId")
    public ResponseEntity<?> getServiceProviderByAuthId(@RequestParam("authId") Long authId) {
        try {
            System.out.println("Received request for ServiceProvider with authId: " + authId);
            ServiceProviderEntity provider = serviceProviderService.getServiceProviderByAuthId(authId);
            if (provider != null) {
                System.out.println("Found provider with ID: " + provider.getProviderId());
                return ResponseEntity.ok(provider);
            } else {
                System.out.println("No provider found for authId: " + authId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No service provider found with authId: " + authId);
            }
        } catch (Exception e) {
            System.err.println("Error in getServiceProviderByAuthId: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching service provider: " + e.getMessage());
        }
    }

    @PutMapping("/update/{providerId}")
    public ServiceProviderEntity updateServiceProvider(@PathVariable Long providerId, @RequestBody ServiceProviderEntity updatedProvider) {
        return serviceProviderService.updateServiceProvider(providerId, updatedProvider);
    }

    @DeleteMapping("/delete/{providerId}")
    public String deleteServiceProvider(@PathVariable Long providerId) {
        return serviceProviderService.deleteServiceProvider(providerId);
    }

    @PostMapping("/uploadServiceProviderImage/{providerId}")
    public ResponseEntity<String> uploadServiceProviderImage(
            @PathVariable Long providerId,
            @RequestParam("image") MultipartFile image) {
        try {
            String result = serviceProviderService.uploadServiceProviderImage(providerId, image);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload service provider profile image.");
        }
    }

    @GetMapping("/getServiceProviderImage/{providerId}")
    public ResponseEntity<String> getServiceProviderImage(@PathVariable Long providerId) {
        try {
            String profileImagePath = serviceProviderService.getServiceProviderImage(providerId);
            return ResponseEntity.ok(profileImagePath);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch service provider profile image.");
        }
    }
}
