package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ReviewEntity;
import edu.cit.serbisyo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // Create a new review
    public ReviewEntity createReview(ReviewEntity review) {
        return reviewRepository.save(review);
    }

    // Get all reviews
    public List<ReviewEntity> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Get a review by ID
    public Optional<ReviewEntity> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    // Get all reviews for a specific service provider
    public List<ReviewEntity> getReviewsByServiceProvider(Long providerId) {
        return reviewRepository.findByServiceProviderId(providerId);
    }

    // Get all reviews from a specific customer
    public List<ReviewEntity> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    // Update a review
    public ReviewEntity updateReview(Long reviewId, ReviewEntity updatedReview) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    review.setRating(updatedReview.getRating());
                    review.setComment(updatedReview.getComment());
                    return reviewRepository.save(review);
                })
                .orElse(null);
    }

    // Delete a review
    public boolean deleteReview(Long reviewId) {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return true;
        }
        return false;
    }
}
