package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.service.ServiceService;
import edu.cit.serbisyo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
