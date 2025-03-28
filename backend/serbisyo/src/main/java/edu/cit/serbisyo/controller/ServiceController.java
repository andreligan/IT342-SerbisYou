package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

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

    // READ BY ID
    @GetMapping("/getById/{serviceId}")
    public ServiceEntity getServiceById(@PathVariable Long serviceId) {
        return serviceService.getServiceById(serviceId);
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
