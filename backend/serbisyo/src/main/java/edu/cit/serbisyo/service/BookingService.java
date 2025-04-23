package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ScheduleService scheduleService;

    public BookingService() {
        super();
    }

    // CREATE a new booking
    public BookingEntity createBooking(BookingEntity booking) {
        // Check if the service provider is available at the requested date and time
        Long providerId = booking.getService().getProvider().getProviderId();
        
        boolean isAvailable = scheduleService.isProviderAvailable(
                providerId, 
                booking.getBookingDate(),
                booking.getBookingTime() != null ? booking.getBookingTime().toString() : null);
        
        if (!isAvailable) {
            throw new IllegalArgumentException("The service provider is not available at the requested time");
        }
        
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

        // Check if the new time is available (if changing date/time)
        if (newBookingDetails.getBookingDate() != null && 
            newBookingDetails.getBookingTime() != null && 
            (!newBookingDetails.getBookingDate().equals(existingBooking.getBookingDate()) ||
             !newBookingDetails.getBookingTime().equals(existingBooking.getBookingTime()))) {
            
            Long providerId = existingBooking.getService().getProvider().getProviderId();
            boolean isAvailable = scheduleService.isProviderAvailable(
                    providerId, 
                    newBookingDetails.getBookingDate(),
                    newBookingDetails.getBookingTime().toString());
            
            if (!isAvailable) {
                throw new IllegalArgumentException("The service provider is not available at the requested time");
            }
        }

        existingBooking.setService(newBookingDetails.getService());
        existingBooking.setBookingDate(newBookingDetails.getBookingDate());
        existingBooking.setBookingTime(newBookingDetails.getBookingTime());
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
