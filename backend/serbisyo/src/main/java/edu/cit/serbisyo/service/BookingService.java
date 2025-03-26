package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository brepo;
    
    public BookingEntity postBookingRecord(BookingEntity booking) {
        return brepo.save(booking);
    }

    public List<BookingEntity> getAllBookings() {
        return brepo.findAll();
    }

    public BookingEntity putBookingDetails(int bookingId, BookingEntity newBookingDetails) {
        return brepo.findById(bookingId).map(booking -> {
            booking.setCustomerId(newBookingDetails.getCustomerId());
            booking.setServiceId(newBookingDetails.getServiceId());
            booking.setBookingDate(newBookingDetails.getBookingDate());
            booking.setStatus(newBookingDetails.getStatus());
            booking.setTotalCost(newBookingDetails.getTotalCost());
            return brepo.save(booking);
        }).orElseThrow(() -> new RuntimeException("Booking ID " + bookingId + " not found."));
    }

    public String deleteBooking(int bookingId) {
        if (brepo.existsById(bookingId)) {
            brepo.deleteById(bookingId);
            return "Booking record successfully deleted.";
        } else {
            return "Booking ID " + bookingId + " not found.";
        }
    }
}
