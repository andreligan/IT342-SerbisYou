package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Create or Update Customer
    public CustomerEntity saveCustomer(CustomerEntity customer) {
        return customerRepository.save(customer);
    }

    // Get All Customers
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    // Get Customer By ID
    public Optional<CustomerEntity> getCustomerById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    // Delete Customer
    public boolean deleteCustomer(Long customerId) {
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            return true;
        }
        return false;
    }
}