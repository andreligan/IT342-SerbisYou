package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceController {

    @Autowired
    private ServiceService sserv;

    @GetMapping("/test")
    public String test() {
        return "Service API is working!";
    }

    // CREATE
    @PostMapping("/postServiceRecord")
    public ServiceEntity postServiceRecord(@RequestBody ServiceEntity service) {
        return sserv.postServiceRecord(service);
    }

    // READ
    @GetMapping("/getAllServices")
    public List<ServiceEntity> getAllServices() {
        return sserv.getAllServices();
    }

    // UPDATE
    @PutMapping("/putServiceDetails")
    public ServiceEntity putServiceDetails(@RequestParam int serviceId, @RequestBody ServiceEntity newServiceDetails) {
        return sserv.putServiceDetails(serviceId, newServiceDetails);
    }

    // DELETE
    @DeleteMapping("/deleteServiceDetails/{serviceId}")
    public String deleteService(@PathVariable int serviceId) {
        return sserv.deleteService(serviceId);
    }
}