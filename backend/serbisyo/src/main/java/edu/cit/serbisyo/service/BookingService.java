package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.NotificationEntity;
import edu.cit.serbisyo.entity.ScheduleEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.NotificationRepository;
import edu.cit.serbisyo.repository.ScheduleRepository;
import edu.cit.serbisyo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private edu.cit.serbisyo.repository.ServiceRepository serviceRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    public BookingService() {
        super();
    }

    // CREATE a new booking
    @Transactional
    public BookingEntity createBooking(BookingEntity booking) {
        // Check if we need to fully load the service with provider information
        if (booking.getService() != null && booking.getService().getProvider() == null) {
            Long serviceId = booking.getService().getServiceId();
            // Load the complete service entity with provider
            edu.cit.serbisyo.entity.ServiceEntity fullService = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NoSuchElementException("Service with ID " + serviceId + " not found"));
            booking.setService(fullService);
        }

        // First save the booking
        BookingEntity savedBooking = bookingRepository.save(booking);

        try {
            // Create notification for service provider
            createBookingNotificationForProvider(savedBooking);
        } catch (Exception e) {
            // Log the error but don't prevent the booking from being created
            System.err.println("Failed to create notification: " + e.getMessage());
        }

        // Get the provider ID from the booking's service
        ServiceEntity service = booking.getService();
        if (service == null || service.getProvider() == null) {
            throw new IllegalStateException("Service or service provider not properly loaded for booking");
        }
        
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

    // Create notification for the service provider
    private void createBookingNotificationForProvider(BookingEntity booking) {
        if (booking == null || booking.getService() == null || booking.getService().getProvider() == null 
                || booking.getService().getProvider().getUserAuth() == null) {
            // Skip notification if any required object is null
            return;
        }
        
        // Create a notification for the service provider
        NotificationEntity notification = new NotificationEntity();
        
        // Set the user (service provider) who will receive the notification
        UserAuthEntity providerUser = booking.getService().getProvider().getUserAuth();
        notification.setUser(providerUser);
        
        // Set notification type and reference information
        notification.setType("booking");
        notification.setReferenceId(booking.getBookingId());
        notification.setReferenceType("Booking");
        
        // Get customer and service information for the message
        String customerName = booking.getCustomer() != null && booking.getCustomer().getUserAuth() != null 
                ? booking.getCustomer().getUserAuth().getUserName() 
                : "A customer";
                
        // Use getServiceName() instead of getTitle() which doesn't exist
        String serviceName = booking.getService().getServiceName();
        
        // Create notification message
        notification.setMessage(customerName + " has booked your service: " + serviceName);
        
        // Set other notification properties
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        // Save the notification
        notificationRepository.save(notification);
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

    /**
     * Get bookings for services offered by a specific provider - enhanced with better null checking
     */
    public List<BookingEntity> getBookingsByProviderId(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID cannot be null");
        }
        
        List<BookingEntity> allBookings = bookingRepository.findAll();
        
        return allBookings.stream()
            .filter(booking -> {
                // Careful null checking for the entire object chain
                if (booking == null) return false;
                
                ServiceEntity service = booking.getService();
                if (service == null) return false;
                
                // Check if service has a provider
                if (service.getProvider() == null) return false;
                
                // Check if provider ID matches
                Long bookingProviderId = service.getProvider().getProviderId();
                return bookingProviderId != null && bookingProviderId.equals(providerId);
            })
            .collect(Collectors.toList());
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

    // Update booking status
    public BookingEntity updateBookingStatus(Long bookingId, String status) {
        BookingEntity booking = getBookingById(bookingId);
        booking.setStatus(status);
        return bookingRepository.save(booking);
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