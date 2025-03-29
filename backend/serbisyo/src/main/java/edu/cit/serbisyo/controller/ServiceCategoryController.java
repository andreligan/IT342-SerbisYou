package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.service.ServiceCategoryService;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/service-categories")
public class ServiceCategoryController {

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @PostMapping("/create")
    public ServiceCategoryEntity createServiceCategory(@RequestBody ServiceCategoryEntity serviceCategory) {
        return serviceCategoryService.createServiceCategory(serviceCategory);
    }

    @GetMapping("/getAll")
    public List<ServiceCategoryEntity> getAllServiceCategories() {
        return serviceCategoryService.getAllServiceCategories();
    }

    @GetMapping("/getById/{categoryId}")
    public ServiceCategoryEntity getServiceCategoryById(@PathVariable Long categoryId) {
        return serviceCategoryService.getServiceCategoryById(categoryId);
    }

    @PutMapping("/update/{categoryId}")
    public ServiceCategoryEntity updateServiceCategory(@PathVariable Long categoryId, @RequestBody ServiceCategoryEntity newCategoryDetails) {
        return serviceCategoryService.updateServiceCategory(categoryId, newCategoryDetails);
    }

    @DeleteMapping("/delete/{categoryId}")
    public String deleteServiceCategory(@PathVariable Long categoryId) {
        return serviceCategoryService.deleteServiceCategory(categoryId);
    }
}
