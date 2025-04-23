package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(path = "/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // In-memory idempotency key store (for demonstration - use a persistent store in production)
    private static final ConcurrentHashMap<String, String> processedIdempotencyKeys = new ConcurrentHashMap<>();

    @GetMapping("/print")
    public String print() {
        return "Booking Controller is working!";
    }

    @PostMapping("/postBooking")
    public ResponseEntity<?> createBooking(
            @RequestBody BookingEntity booking,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        try {
            // Check for idempotency key to prevent duplicate bookings
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                // If this key has been processed before, return the previous response
                if (processedIdempotencyKeys.containsKey(idempotencyKey)) {
                    return ResponseEntity.ok().body(Map.of(
                        "message", "Booking already processed",
                        "status", "DUPLICATE",
                        "bookingId", processedIdempotencyKeys.get(idempotencyKey)
                    ));
                }
            }

            // Create the booking
            BookingEntity savedBooking = bookingService.saveBooking(booking);

            // Store the idempotency key and booking ID for future reference
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                processedIdempotencyKeys.put(idempotencyKey, savedBooking.getBookingId().toString());
            }

            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
    public ResponseEntity<?> updateBooking(@PathVariable Long bookingId, @RequestBody BookingEntity updatedBooking) {
        try {
            BookingEntity booking = bookingService.updateBooking(bookingId, updatedBooking);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the booking: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId) {
        return bookingService.deleteBooking(bookingId);
    }
}
