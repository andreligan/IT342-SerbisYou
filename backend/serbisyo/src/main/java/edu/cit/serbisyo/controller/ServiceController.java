package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.service.ServiceService;
import edu.cit.serbisyo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/print")
    public String print() {
        return "Service Controller is working!";
    }

    // GET SERVICES BY PROVIDER ID
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getServicesByProviderId(@PathVariable Long providerId) {
        try {
            List<ServiceEntity> services = serviceService.getServicesByProviderId(providerId);
            if (services.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>()); // Return empty list instead of 404
            }
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching services for provider: " + e.getMessage());
        }
    }
    
    // GET SERVICES BY PROVIDER ID WITH RATINGS
    @GetMapping("/provider/{providerId}/withRatings")
    public ResponseEntity<?> getServicesByProviderIdWithRatings(@PathVariable Long providerId) {
        try {
            List<ServiceEntity> services = serviceService.getServicesByProviderId(providerId);
            List<Map<String, Object>> servicesWithRatings = new ArrayList<>();
            
            for (ServiceEntity service : services) {
                Map<String, Object> serviceMap = new HashMap<>();
                serviceMap.put("service", service);
                serviceMap.put("rating", reviewService.getServiceRating(service.getServiceId()));
                servicesWithRatings.add(serviceMap);
            }
            
            return ResponseEntity.ok(servicesWithRatings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching services with ratings for provider: " + e.getMessage());
        }
    }

    // CREATE
    @PostMapping("/postService/{providerId}/{categoryId}")
    public ServiceEntity createService(
            @PathVariable Long providerId,
            @PathVariable Long categoryId,
            @RequestBody ServiceEntity serviceDetails) {
        return serviceService.createService(providerId, categoryId, serviceDetails);
    }

    // READ ALL
    @GetMapping("/getAll")
    public List<ServiceEntity> getAllServices() {
        return serviceService.getAllServices();
    }

    // READ ALL with ratings
    @GetMapping("/getAllWithRatings")
    public List<Map<String, Object>> getAllServicesWithRatings() {
        List<ServiceEntity> services = serviceService.getAllServices();
        List<Map<String, Object>> servicesWithRatings = new ArrayList<>();
        
        for (ServiceEntity service : services) {
            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put("service", service);
            serviceMap.put("rating", reviewService.getServiceRating(service.getServiceId()));
            servicesWithRatings.add(serviceMap);
        }
        
        return servicesWithRatings;
    }

    // READ BY ID
    @GetMapping("/getById/{serviceId}")
    public ServiceEntity getServiceById(@PathVariable Long serviceId) {
        return serviceService.getServiceById(serviceId);
    }

    // READ BY ID with rating
    @GetMapping("/getByIdWithRating/{serviceId}")
    public Map<String, Object> getServiceByIdWithRating(@PathVariable Long serviceId) {
        ServiceEntity service = serviceService.getServiceById(serviceId);
        Map<String, Object> result = new HashMap<>();
        
        if (service != null) {
            result.put("service", service);
            result.put("rating", reviewService.getServiceRating(serviceId));
        }
        
        return result;
    }

    // UPDATE
    @PutMapping("/updateService/{serviceId}/{providerId}/{categoryId}")
    public ServiceEntity updateService(
            @PathVariable Long serviceId,
            @PathVariable Long providerId,
            @PathVariable Long categoryId,
            @RequestBody ServiceEntity newServiceDetails) {
        return serviceService.updateService(serviceId, providerId, categoryId, newServiceDetails);
    }

    // DELETE
    @DeleteMapping("/delete/{serviceId}")
    public String deleteService(@PathVariable Long serviceId) {
        return serviceService.deleteService(serviceId);
    }

    @PostMapping("/uploadServiceImage/{serviceId}")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long serviceId,
            @RequestParam("image") MultipartFile image) {
        try {
            String result = serviceService.uploadServiceImage(serviceId, image);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
        }
    }

    @GetMapping("/getServiceImage/{serviceId}")
    public ResponseEntity<String> getServiceImage(@PathVariable Long serviceId) {
        try {
            String profileImagePath = serviceService.getServiceImage(serviceId);
            return ResponseEntity.ok(profileImagePath);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch profile image.");
        }
    }
}
