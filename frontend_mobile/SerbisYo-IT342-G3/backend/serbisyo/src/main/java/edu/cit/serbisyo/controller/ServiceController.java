package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.service.ServiceService;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // Get all services
    @GetMapping("/getAll")
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        try {
            List<ServiceEntity> services = serviceService.getAllServices();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get service by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable("id") Long id) {
        try {
            Optional<ServiceEntity> serviceData = serviceService.getServiceById(id);
            if (serviceData.isPresent()) {
                return new ResponseEntity<>(serviceData.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create a new service
    @PostMapping("/postService/{providerId}/{categoryId}")
    public ResponseEntity<ServiceEntity> createService(@PathVariable("providerId") Long providerId,
                                                   @PathVariable("categoryId") Long categoryId,
                                                   @RequestBody Map<String, Object> serviceDetails) {
        try {
            String serviceName = (String) serviceDetails.get("serviceName");
            String serviceDescription = (String) serviceDetails.get("serviceDescription");
            String priceRange = (String) serviceDetails.get("priceRange");
            String durationEstimate = (String) serviceDetails.get("durationEstimate");
            
            ServiceEntity service = serviceService.createService(
                providerId, categoryId, serviceName, serviceDescription, priceRange, durationEstimate
            );
            
            return new ResponseEntity<>(service, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update a service
    @PutMapping("/updateService/{id}/{providerId}/{categoryId}")
    public ResponseEntity<ServiceEntity> updateService(@PathVariable("id") Long id,
                                                   @PathVariable("providerId") Long providerId,
                                                   @PathVariable("categoryId") Long categoryId,
                                                   @RequestBody Map<String, Object> serviceDetails) {
        try {
            String serviceName = (String) serviceDetails.get("serviceName");
            String serviceDescription = (String) serviceDetails.get("serviceDescription");
            String priceRange = (String) serviceDetails.get("priceRange");
            String durationEstimate = (String) serviceDetails.get("durationEstimate");
            
            ServiceEntity updatedService = serviceService.updateService(
                id, providerId, categoryId, serviceName, serviceDescription, priceRange, durationEstimate
            );
            
            if (updatedService != null) {
                return new ResponseEntity<>(updatedService, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a service
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteService(@PathVariable("id") Long id) {
        try {
            boolean success = serviceService.deleteService(id);
            if (success) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete all services (for testing/cleanup)
    @DeleteMapping("/deleteAll")
    public ResponseEntity<HttpStatus> deleteAllServices() {
        try {
            serviceService.deleteAllServices();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 