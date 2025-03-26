package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.service.CustomerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Create Customer
    @PostMapping("/createCustomers")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerEntity customer) {
        CustomerEntity savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.status(201).body(savedCustomer);
    }

    // Get All Customers
    @GetMapping
    public List<CustomerEntity> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // Update Customer
    @PutMapping("/{customerId}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long customerId, @RequestBody CustomerEntity updatedCustomer) {
        Optional<CustomerEntity> customerData = customerService.getCustomerById(customerId);

        if (customerData.isPresent()) {
            CustomerEntity existingCustomer = customerData.get();
            existingCustomer.setUserId(updatedCustomer.getUserId());
            existingCustomer.setAddressId(updatedCustomer.getAddressId());
            existingCustomer.setFirstName(updatedCustomer.getFirstName());
            existingCustomer.setLastName(updatedCustomer.getLastName());
            existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
            existingCustomer.setEmail(updatedCustomer.getEmail());

            CustomerEntity savedCustomer = customerService.saveCustomer(existingCustomer);
            return ResponseEntity.ok(savedCustomer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete Customer
    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long customerId) {
        boolean isDeleted = customerService.deleteCustomer(customerId);
        if (isDeleted) {
            return ResponseEntity.ok().body("Customer deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}