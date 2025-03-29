package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}