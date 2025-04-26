package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.CustomerEntity;
import edu.cit.serbisyo.entity.ReviewEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.CustomerRepository;
import edu.cit.serbisyo.repository.ReviewRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import edu.cit.serbisyo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    public ReviewEntity createReview(ReviewEntity review) {
        // Log the received review details
        System.out.println("=== Creating New Review ===");
        System.out.println("Review Object: " + review);
        
        if (review.getCustomer() != null) {
            System.out.println("Customer ID: " + review.getCustomer().getCustomerId());
        } else {
            System.out.println("Customer is null!");
        }
        
        if (review.getProvider() != null) {
            System.out.println("Provider ID: " + review.getProvider().getProviderId());
        } else {
            System.out.println("Provider is null!");
        }
        
        if (review.getBooking() != null) {
            System.out.println("Booking ID: " + review.getBooking().getBookingId());
        } else {
            System.out.println("Booking is null!");
        }
        
        System.out.println("Rating: " + review.getRating());
        System.out.println("Comment: " + review.getComment());
        System.out.println("Date: " + review.getReviewDate());
        System.out.println("=== End Review Details ===");
        
        return reviewRepository.save(review);
    }

    public ReviewEntity createReviewWithIds(Long customerId, Long providerId, Long bookingId, int rating, String comment, String reviewDateStr) {
        System.out.println("Creating review with IDs - Customer: " + customerId + ", Provider: " + providerId + ", Booking: " + bookingId);
        
        // Get entities from repositories
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        
        // Create and populate review entity
        ReviewEntity review = new ReviewEntity();
        review.setCustomer(customer);
        review.setProvider(provider);
        review.setBooking(booking);
        review.setRating(rating);
        review.setComment(comment);
        
        // Parse date
        try {
            review.setReviewDate(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(reviewDateStr));
        } catch (Exception e) {
            review.setReviewDate(new java.util.Date());
            System.out.println("Could not parse date, using current time instead: " + e.getMessage());
        }
        
        return createReview(review);
    }

    public List<ReviewEntity> getReviewsByProvider(Long providerId) {
        return reviewRepository.findByProviderProviderId(providerId);
    }

    public ReviewEntity updateReview(Long reviewId, ReviewEntity updatedReview) {
        ReviewEntity existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        // Update fields
        if (updatedReview.getRating() > 0) {
            existingReview.setRating(updatedReview.getRating());
        }
        
        if (updatedReview.getComment() != null) {
            existingReview.setComment(updatedReview.getComment());
        }
        
        return reviewRepository.save(existingReview);
    }

    public String deleteReview(Long reviewId) {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return "Review deleted successfully";
        }
        return "Review not found";
    }

    public List<ReviewEntity> getReviewsByService(Long serviceId) {
        // First get all bookings for this service ID
        List<BookingEntity> bookings = bookingRepository.findByServiceServiceId(serviceId);
        
        // Then collect all reviews for those bookings
        List<ReviewEntity> reviews = new ArrayList<>();
        for (BookingEntity booking : bookings) {
            List<ReviewEntity> bookingReviews = reviewRepository.findByBookingBookingId(booking.getBookingId());
            if (bookingReviews != null && !bookingReviews.isEmpty()) {
                reviews.addAll(bookingReviews);
            }
        }
        
        return reviews;
    }
    
    public Map<String, Object> getServiceRating(Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ReviewEntity> reviews = getReviewsByService(serviceId);
        
        if (reviews.isEmpty()) {
            result.put("averageRating", 0.0);
            result.put("reviewCount", 0);
            return result;
        }
        
        // Calculate average rating
        double sum = 0;
        for (ReviewEntity review : reviews) {
            sum += review.getRating();
        }
        
        double averageRating = sum / reviews.size();
        
        // Round to 1 decimal place
        double roundedRating = Math.round(averageRating * 10) / 10.0;
        
        result.put("averageRating", roundedRating);
        result.put("reviewCount", reviews.size());
        
        return result;
    }

    /**
     * Check if a customer has already reviewed a specific booking
     * 
     * @param customerId The ID of the customer
     * @param bookingId The ID of the booking
     * @return true if the customer has already reviewed this booking, false otherwise
     */
    public boolean hasCustomerReviewedBooking(Long customerId, Long bookingId) {
        List<ReviewEntity> reviewsForBooking = reviewRepository.findByBookingBookingId(bookingId);
        return reviewsForBooking.stream()
                .anyMatch(review -> review.getCustomer() != null && 
                        review.getCustomer().getCustomerId().equals(customerId));
    }
}