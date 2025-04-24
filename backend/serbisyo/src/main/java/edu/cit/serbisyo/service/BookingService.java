package edu.cit.serbisyo.service;
 
import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.util.List;
import java.util.NoSuchElementException;
 
@Service
public class BookingService {
 
    @Autowired
    private BookingRepository bookingRepository;
 
    public BookingService() {
        super();
    }
 
    // CREATE a new booking
    public BookingEntity createBooking(BookingEntity booking) {
        return bookingRepository.save(booking);
    }
 
    // READ all bookings
    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }
 
    // READ a booking by ID
    public BookingEntity getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking with ID " + bookingId + " not found"));
    }
 
    // UPDATE an existing booking
    public BookingEntity updateBooking(Long bookingId, BookingEntity newBookingDetails) {
        BookingEntity existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking with ID " + bookingId + " not found"));
 
//        existingBooking.setCustomer(newBookingDetails.getCustomer());
        existingBooking.setService(newBookingDetails.getService());
        existingBooking.setBookingDate(newBookingDetails.getBookingDate());
        existingBooking.setTotalCost(newBookingDetails.getTotalCost());
        existingBooking.setStatus(newBookingDetails.getStatus());
 
        return bookingRepository.save(existingBooking);
    }
 
    // DELETE a booking
    public String deleteBooking(Long bookingId) {
        if (bookingRepository.existsById(bookingId)) {
            bookingRepository.deleteById(bookingId);
            return "Booking with ID " + bookingId + " has been deleted successfully.";
        } else {
            return "Booking with ID " + bookingId + " not found.";
        }
    }
}