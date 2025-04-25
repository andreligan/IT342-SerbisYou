package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.ScheduleEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.ScheduleRepository;
import edu.cit.serbisyo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BookingService() {
        super();
    }

    // CREATE a new booking
    @Transactional
    public BookingEntity createBooking(BookingEntity booking) {
        // First save the booking
        BookingEntity savedBooking = bookingRepository.save(booking);

        // Get the provider ID from the booking's service
        ServiceEntity service = booking.getService();
        Long providerId = service.getProvider().getProviderId();

        // Get the day of week from the booking date
        DayOfWeek dayOfWeek = booking.getBookingDate().getDayOfWeek();

        // Find the schedule slot that exactly matches this booking time (using start time)
        List<ScheduleEntity> schedules = scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndStartTime(
                providerId, dayOfWeek, booking.getBookingTime());

        // If no exact match found, try the previous query as fallback
        if (schedules.isEmpty()) {
            schedules = scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                    providerId, dayOfWeek, booking.getBookingTime(), booking.getBookingTime());
        }

        // If a matching schedule is found, update it to not available
        if (!schedules.isEmpty()) {
            ScheduleEntity schedule = schedules.get(0);
            schedule.setAvailable(false);
            scheduleRepository.save(schedule);
        }

        // Create transaction record for any payment method
        if (booking.getPaymentMethod() != null) {
            if (booking.getPaymentMethod().equalsIgnoreCase("gcash")) {
                createTransactionForBooking(savedBooking, booking.isFullPayment());
            } else if (booking.getPaymentMethod().equalsIgnoreCase("cash")) {
                createCashTransactionForBooking(savedBooking);
            }
        }

        return savedBooking;
    }

    // Helper method to create a transaction record
    private TransactionEntity createTransactionForBooking(BookingEntity booking, boolean isFullPayment) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setBooking(booking);
        transaction.setPaymentMethod("GCash");

        // Calculate amount based on whether it's full payment or downpayment
        double amount = isFullPayment ? booking.getTotalCost() : (booking.getTotalCost() * 0.5);
        transaction.setAmount(amount);

        // Set status based on payment type
        transaction.setStatus(isFullPayment ? "COMPLETED" : "PARTIAL");
        transaction.setTransactionDate(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // Helper method to create a transaction record for cash payments
    private TransactionEntity createCashTransactionForBooking(BookingEntity booking) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setBooking(booking);
        transaction.setPaymentMethod("Cash");
        transaction.setAmount(booking.getTotalCost());
        transaction.setStatus("PENDING"); // Cash payments are pending until completed
        transaction.setTransactionDate(null); // Will be set when payment is received

        return transactionRepository.save(transaction);
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

        // Handle payment status updates if needed
        if (newBookingDetails.getPaymentMethod() != null &&
                "gcash".equalsIgnoreCase(newBookingDetails.getPaymentMethod()) &&
                "COMPLETED".equalsIgnoreCase(newBookingDetails.getStatus())) {
            // This might be where you'd update transaction status if needed
        }

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

        // Find the schedule slot that exactly matches this booking time (using start time)
        List<ScheduleEntity> schedules = scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndStartTime(
                providerId, dayOfWeek, booking.getBookingTime());

        // If no exact match found, try the previous query as fallback
        if (schedules.isEmpty()) {
            schedules = scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                    providerId, dayOfWeek, booking.getBookingTime(), booking.getBookingTime());
        }

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