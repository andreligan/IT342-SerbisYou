package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
    
    @Autowired
    private BookingService bserv;

    @PostMapping("/postBookingRecord")
    public BookingEntity postBookingRecord(@RequestBody BookingEntity booking) {
        return bserv.postBookingRecord(booking);
    }

    @GetMapping("/getAllBookings")
    public List<BookingEntity> getAllBookings() {
        return bserv.getAllBookings();
    }

    @PutMapping("/putBookingDetails")
    public BookingEntity putBookingDetails(@RequestParam int bookingId, @RequestBody BookingEntity newBookingDetails) {
        return bserv.putBookingDetails(bookingId, newBookingDetails);
    }

    @DeleteMapping("/deleteBookingDetails/{bookingId}")
    public String deleteBooking(@PathVariable int bookingId) {
        return bserv.deleteBooking(bookingId);
    }
}