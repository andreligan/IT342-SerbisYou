package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.service.ServiceCategoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/serviceCategory")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceCategoryController {

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    // CREATE
    @PostMapping("/add")
    public ServiceCategoryEntity addServiceCategory(@RequestBody ServiceCategoryEntity serviceCategory) {
        return serviceCategoryService.addServiceCategory(serviceCategory);
    }

    // READ
    @GetMapping("/getAll")
    public List<ServiceCategoryEntity> getAllServiceCategories() {
        return serviceCategoryService.getAllServiceCategories();
    }

    @GetMapping("/get/{id}")
    public Optional<ServiceCategoryEntity> getServiceCategoryById(@PathVariable int id) {
        return serviceCategoryService.getServiceCategoryById(id);
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ServiceCategoryEntity updateServiceCategory(@PathVariable int id, @RequestBody ServiceCategoryEntity newDetails) {
        return serviceCategoryService.updateServiceCategory(id, newDetails);
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public String deleteServiceCategory(@PathVariable int id) {
        return serviceCategoryService.deleteServiceCategory(id);
    }
}