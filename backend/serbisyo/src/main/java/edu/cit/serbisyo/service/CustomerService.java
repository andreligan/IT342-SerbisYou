package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.repository.CustomerRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public String uploadProfileImage(Long customerId, MultipartFile image) throws IOException {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Ensure the uploads directory exists
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir); // Create the directory if it doesn't exist
        }
        
        // Save the image to a local directory or cloud storage
        String fileName = customerId + "_" + image.getOriginalFilename();
        Path filePath = Paths.get("uploads/" + fileName);
        Files.write(filePath, image.getBytes());

        // Save the file path to the database
        customer.setProfileImage(filePath.toString());
        customerRepository.save(customer);

        return "Profile image uploaded successfully.";
    }

    public String getProfileImage(Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    
        if (customer.getProfileImage() == null || customer.getProfileImage().isEmpty()) {
            throw new IllegalArgumentException("Profile image not found for the customer.");
        }

        System.out.println("Profile Image Path: " + "/uploads/" + Paths.get(customer.getProfileImage()).getFileName().toString());
    
        // Return the relative path of the profile image
        return "/uploads/" + Paths.get(customer.getProfileImage()).getFileName().toString();
    }
}