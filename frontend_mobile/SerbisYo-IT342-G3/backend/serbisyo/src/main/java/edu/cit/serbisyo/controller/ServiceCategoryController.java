package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import edu.cit.serbisyo.entity.ServiceCategoryEntity;
import edu.cit.serbisyo.service.ServiceCategoryService;

@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryController {

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @GetMapping("/getAll")
    public ResponseEntity<List<ServiceCategoryEntity>> getAllCategories() {
        try {
            List<ServiceCategoryEntity> categories = serviceCategoryService.getAllCategories();
            return new ResponseEntity<>(categories, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServiceCategoryEntity> getCategoryById(@PathVariable("id") Long id) {
        try {
            Optional<ServiceCategoryEntity> categoryData = serviceCategoryService.getCategoryById(id);
            if (categoryData.isPresent()) {
                return new ResponseEntity<>(categoryData.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ServiceCategoryEntity> createCategory(@RequestBody ServiceCategoryEntity category) {
        try {
            ServiceCategoryEntity createdCategory = serviceCategoryService.createCategory(category);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ServiceCategoryEntity> updateCategory(@PathVariable("id") Long id, @RequestBody ServiceCategoryEntity category) {
        try {
            ServiceCategoryEntity updatedCategory = serviceCategoryService.updateCategory(id, category);
            if (updatedCategory != null) {
                return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteCategory(@PathVariable("id") Long id) {
        try {
            boolean success = serviceCategoryService.deleteCategory(id);
            if (success) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 