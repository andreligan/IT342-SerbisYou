package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/update/{providerId}")
    public ServiceProviderEntity updateServiceProvider(@PathVariable Long providerId, @RequestBody ServiceProviderEntity updatedProvider) {
        return serviceProviderService.updateServiceProvider(providerId, updatedProvider);
    }

    @DeleteMapping("/delete/{providerId}")
    public String deleteServiceProvider(@PathVariable Long providerId) {
        return serviceProviderService.deleteServiceProvider(providerId);
    }
}
