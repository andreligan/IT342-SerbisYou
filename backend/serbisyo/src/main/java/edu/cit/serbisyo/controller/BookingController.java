package edu.cit.serbisyo.controller;
 
import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/bookings")
public class BookingController {
 
    @Autowired
    private BookingService bookingService;
 
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
}
 