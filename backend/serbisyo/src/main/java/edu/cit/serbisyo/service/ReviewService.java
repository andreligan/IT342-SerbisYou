package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.entity.ReviewEntity;
import edu.cit.serbisyo.entity.ServiceEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.ReviewRepository;
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

    public ReviewEntity createReview(ReviewEntity review) {
        return reviewRepository.save(review);
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
}