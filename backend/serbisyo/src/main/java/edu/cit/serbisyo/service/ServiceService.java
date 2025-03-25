package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository srepo;

    public ServiceService() {
        super();
    }

    // CREATE
    public ServiceEntity postServiceRecord(ServiceEntity service) {
        return srepo.save(service);
    }

    // READ
    public List<ServiceEntity> getAllServices() {
        return srepo.findAll();
    }

    // UPDATE
    public ServiceEntity putServiceDetails(int serviceId, ServiceEntity newServiceDetails) {
        return srepo.findById(serviceId)
                .map(existingService -> {
                    existingService.setServiceName(newServiceDetails.getServiceName());
                    existingService.setDescription(newServiceDetails.getDescription());
                    existingService.setPriceRange(newServiceDetails.getPriceRange());
                    existingService.setDurationEstimate(newServiceDetails.getDurationEstimate());
                    return srepo.save(existingService);
                }).orElseThrow(() -> new NoSuchElementException("Service ID " + serviceId + " not found."));
    }

    // DELETE
    public String deleteService(int serviceId) {
        if (srepo.existsById(serviceId)) {
            srepo.deleteById(serviceId);
            return "Service record successfully deleted.";
        }
        return "Service ID " + serviceId + " not found.";
    }
}