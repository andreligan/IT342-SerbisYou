package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.repository.CustomerRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public CustomerService() {
        super();
    }

    // CREATE
    public CustomerEntity createCustomer(CustomerEntity customer) {
        return customerRepository.save(customer);
    }

    // READ ALL
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    // READ BY ID
    public Optional<CustomerEntity> getCustomerById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    // UPDATE
    public CustomerEntity updateCustomer(Long customerId, CustomerEntity updatedCustomer) {
        CustomerEntity existingCustomer = customerRepository.findById(customerId).orElseThrow(() ->
                new NoSuchElementException("Customer with ID " + customerId + " not found"));

//        existingCustomer.setUserAuth(updatedCustomer.getUserAuth());
        existingCustomer.setAddress(updatedCustomer.getAddress());
        existingCustomer.setFirstName(updatedCustomer.getFirstName());
        existingCustomer.setLastName(updatedCustomer.getLastName());
        existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());

        return customerRepository.save(existingCustomer);
    }

    // DELETE
    public String deleteCustomer(Long customerId) {
        if (customerRepository.findById(customerId).isPresent()) {
            customerRepository.deleteById(customerId);
            return "Customer successfully deleted.";
        } else {
            return "Customer with ID " + customerId + " not found.";
        }
    }
}