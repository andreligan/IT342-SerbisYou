package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ReviewEntity;
import edu.cit.serbisyo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public ReviewEntity createReview(ReviewEntity review) {
        return reviewRepository.save(review);
    }

    public List<ReviewEntity> getReviewsByProvider(Long providerId) {
        return reviewRepository.findByProviderProviderId(providerId);
    }

    public ReviewEntity updateReview(Long reviewId, ReviewEntity updatedReview) {
        ReviewEntity existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());
        return reviewRepository.save(existingReview);
    }

    public String deleteReview(Long reviewId) {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return "Review successfully deleted.";
        }
        return "Review not found.";
    }
}