package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.ScheduleEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public BookingService() {
        super();
    }

    // CREATE a new booking
    public BookingEntity createBooking(BookingEntity booking) {
        // First save the booking
        BookingEntity savedBooking = bookingRepository.save(booking);

        // Get the provider ID from the booking's service
        ServiceEntity service = booking.getService();
        Long providerId = service.getProvider().getProviderId();

        // Get the day of week from the booking date
        DayOfWeek dayOfWeek = booking.getBookingDate().getDayOfWeek();

        // Find the schedule slot that matches this booking
        List<ScheduleEntity> schedules = scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                providerId, dayOfWeek, booking.getBookingTime(), booking.getBookingTime());

        // If a matching schedule is found, update it to not available
        if (!schedules.isEmpty()) {
            ScheduleEntity schedule = schedules.get(0);
            schedule.setAvailable(false);
            scheduleRepository.save(schedule);
        }

        return savedBooking;
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

    // Get bookings by customer ID
    public List<BookingEntity> getBookingsByCustomerId(Long customerId) {
        return bookingRepository.findByCustomerCustomerId(customerId);
    }

    // UPDATE an existing booking
    public BookingEntity updateBooking(Long bookingId, BookingEntity newBookingDetails) {
        BookingEntity existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking with ID " + bookingId + " not found"));

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

    /**
     * Complete a booking and update the provider's schedule
     */
    public BookingEntity completeBookingAndReleaseSchedule(Long bookingId) {
        // Get the booking
        BookingEntity booking = getBookingById(bookingId);

        // Update status to completed
        booking.setStatus("COMPLETED");

        // Get the provider ID from the booking's service
        ServiceEntity service = booking.getService();
        Long providerId = service.getProvider().getProviderId();

        // Get the day of week from the booking date
        DayOfWeek dayOfWeek = booking.getBookingDate().getDayOfWeek();

        // Find the schedule slot that matches this booking
        List<ScheduleEntity> schedules = scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                providerId, dayOfWeek, booking.getBookingTime(), booking.getBookingTime());

        // If a matching schedule is found, update it to available
        if (!schedules.isEmpty()) {
            ScheduleEntity schedule = schedules.get(0);
            schedule.setAvailable(true);
            scheduleRepository.save(schedule);
        }

        // Save the updated booking
        return bookingRepository.save(booking);
    }
}