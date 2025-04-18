package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/print")
    public String print() {
        return "Customer Controller is working!";
    }

    // CREATE
    @PostMapping("/postCustomer")
    public CustomerEntity createCustomer(@RequestBody CustomerEntity customer) {
        return customerService.createCustomer(customer);
    }

    // READ ALL
    @GetMapping("/getAll")
    public List<CustomerEntity> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // READ BY ID
    @GetMapping("/getById/{customerId}")
    public CustomerEntity getCustomerById(@PathVariable Long customerId) {
        return customerService.getCustomerById(customerId)
                .orElse(null); // Return null if not found, modify as needed
    }

    // UPDATE
    @PutMapping("/updateCustomer/{customerId}")
    public CustomerEntity updateCustomer(@PathVariable Long customerId, @RequestBody CustomerEntity updatedCustomer) {
        return customerService.updateCustomer(customerId, updatedCustomer);
    }

    // DELETE
    @DeleteMapping("/delete/{customerId}")
    public String deleteCustomer(@PathVariable Long customerId) {
        return customerService.deleteCustomer(customerId);
    }

    @PostMapping("/upload-image/{customerId}")
    public ResponseEntity<String> uploadProfileImage(
            @PathVariable Long customerId,
            @RequestParam("image") MultipartFile image) {
        try {
            String result = customerService.uploadProfileImage(customerId, image);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
        }
    }

    @GetMapping("/getProfileImage/{customerId}")
    public ResponseEntity<String> getProfileImage(@PathVariable Long customerId) {
        try {
            String profileImagePath = customerService.getProfileImage(customerId);
            return ResponseEntity.ok(profileImagePath);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch profile image.");
        }
    }
}