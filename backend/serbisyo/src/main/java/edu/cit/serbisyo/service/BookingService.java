package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.CustomerRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.ServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    // Create booking with proper entity relationships
    @Transactional
    public BookingEntity createBooking(BookingEntity booking) {
        if (booking.getCustomer() == null || booking.getCustomer().getCustomerId() == null) {
            throw new IllegalArgumentException("Customer information is required");
        }
        if (booking.getService() == null || booking.getService().getServiceId() == null) {
            throw new IllegalArgumentException("Service information is required");
        }
        
        // Get full customer entity
        Optional<CustomerEntity> customerOpt = customerRepository.findById(booking.getCustomer().getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new IllegalArgumentException("Customer with ID " + booking.getCustomer().getCustomerId() + " not found");
        }
        
        // Get full service entity
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(booking.getService().getServiceId());
        if (!serviceOpt.isPresent()) {
            throw new IllegalArgumentException("Service with ID " + booking.getService().getServiceId() + " not found");
        }
        
        // Get the full service entity with its relationships
        ServiceEntity fullService = serviceOpt.get();
        
        // Check if provider is properly loaded - if not, load it explicitly
        if (fullService.getProvider() == null) {
            // Log the issue for debugging
            System.out.println("WARNING: Service provider is null for service ID: " + fullService.getServiceId());
            
            // Find the provider directly for this service - this is a workaround
            // You might need to modify the ServiceRepository to have a method to find the provider for a service
            // For now, let's just load all services and find the matching one to get its provider
            List<ServiceEntity> allServices = serviceRepository.findAll();
            for (ServiceEntity s : allServices) {
                if (s.getServiceId().equals(fullService.getServiceId()) && s.getProvider() != null) {
                    fullService.setProvider(s.getProvider());
                    System.out.println("Fixed provider relationship for service: " + s.getServiceId());
                    break;
                }
            }
            
            // If we still don't have a provider, throw an exception
            if (fullService.getProvider() == null) {
                throw new IllegalArgumentException("Service provider information is missing for service ID: " + fullService.getServiceId());
            }
        }
        
        // Set the complete entities in the booking
        booking.setCustomer(customerOpt.get());
        booking.setService(fullService);
        
        // Save the booking
        return bookingRepository.save(booking);
    }

    // Get all bookings
    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Get booking by ID
    public BookingEntity getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));
    }

    // Update booking
    @Transactional
    public BookingEntity updateBooking(Long bookingId, BookingEntity updatedBooking) {
        BookingEntity existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));

        // Update the fields
        if (updatedBooking.getBookingDate() != null) {
            existingBooking.setBookingDate(updatedBooking.getBookingDate());
        }
        
        if (updatedBooking.getBookingTime() != null) {
            existingBooking.setBookingTime(updatedBooking.getBookingTime());
        }
        
        if (updatedBooking.getStatus() != null) {
            existingBooking.setStatus(updatedBooking.getStatus());
        }
        
        if (updatedBooking.getTotalCost() > 0) {
            existingBooking.setTotalCost(updatedBooking.getTotalCost());
        }
        
        if (updatedBooking.getNote() != null) {
            existingBooking.setNote(updatedBooking.getNote());
        }

        return bookingRepository.save(existingBooking);
    }

    // Delete booking
    public String deleteBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NoSuchElementException("Booking not found with ID: " + bookingId);
        }
        bookingRepository.deleteById(bookingId);
        return "Booking deleted successfully";
    }
}
