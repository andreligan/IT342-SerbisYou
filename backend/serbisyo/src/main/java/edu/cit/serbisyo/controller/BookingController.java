package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.CustomerRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @GetMapping("/print")
    public String print() {
        return "Booking Controller is working!";
    }

    @PostMapping("/postBooking")
    public BookingEntity createBooking(@RequestBody BookingEntity booking) {
        return bookingService.createBooking(booking);
    }

    @GetMapping("/getAll")
    public List<BookingEntity> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/getById/{bookingId}")
    public BookingEntity getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @PutMapping("/updateBooking/{bookingId}")
    public BookingEntity updateBooking(@PathVariable Long bookingId, @RequestBody BookingEntity updatedBooking) {
        return bookingService.updateBooking(bookingId, updatedBooking);
    }

    @DeleteMapping("/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId) {
        return bookingService.deleteBooking(bookingId);
    }

    @GetMapping("/getCustomerBookings")
    public ResponseEntity<?> getCustomerBookings() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Find the customer by username
            Optional<CustomerEntity> customerOpt = customerRepository.findByUserAuthUserName(username);

            if (!customerOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found for the authenticated user");
            }

            Long customerId = customerOpt.get().getCustomerId();

            // Get bookings for this customer
            List<BookingEntity> bookings = bookingService.getBookingsByCustomerId(customerId);
            return ResponseEntity.ok(bookings);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving bookings: " + e.getMessage());
        }
    }

    @PutMapping("/complete/{bookingId}")
    public ResponseEntity<BookingEntity> completeBooking(@PathVariable Long bookingId) {
        try {
            BookingEntity completedBooking = bookingService.completeBookingAndReleaseSchedule(bookingId);
            return ResponseEntity.ok(completedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getProviderBookings")
    public ResponseEntity<?> getProviderBookings() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }
            
            String username = authentication.getName();
            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username not available");
            }

            // Find the service provider by username
            Optional<ServiceProviderEntity> providerOpt = serviceProviderRepository.findByUserAuthUserName(username);

            if (!providerOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service provider not found for user: " + username);
            }

            Long providerId = providerOpt.get().getProviderId();
            if (providerId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provider ID is null for user: " + username);
            }

            // Get bookings for services offered by this provider
            List<BookingEntity> bookings = bookingService.getBookingsByProviderId(providerId);
            return ResponseEntity.ok(bookings);

        } catch (Exception e) {
            // Log the full exception for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving bookings: " + e.getMessage());
        }
    }

    @PutMapping("/updateStatus/{bookingId}")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long bookingId, @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body("Status is required");
            }
            
            BookingEntity updatedBooking = bookingService.updateBookingStatus(bookingId, newStatus);
            return ResponseEntity.ok(updatedBooking);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating booking status: " + e.getMessage());
        }
    }
}
