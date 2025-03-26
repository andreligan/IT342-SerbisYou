package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ReviewEntity;
import edu.cit.serbisyo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Create a review
    @PostMapping
    public ResponseEntity<ReviewEntity> createReview(@RequestBody ReviewEntity review) {
        ReviewEntity savedReview = reviewService.createReview(review);
        return ResponseEntity.ok(savedReview);
    }

    // Get all reviews
    @GetMapping
    public ResponseEntity<List<ReviewEntity>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // Get a review by ID
    @GetMapping("/getReview/{reviewId}")
    public ResponseEntity<ReviewEntity> getReviewById(@PathVariable int reviewId) {
        Optional<ReviewEntity> review = reviewService.getReviewById(reviewId);
        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get all reviews by service provider ID
    @GetMapping("/getAllReview/{providerId}")
    public ResponseEntity<List<ReviewEntity>> getReviewsByServiceProvider(@PathVariable int providerId) {
        return ResponseEntity.ok(reviewService.getReviewsByServiceProvider(providerId));
    }

    // Get all reviews by customer ID
    //@GetMapping("/customer/{customerId}")
    //public ResponseEntity<List<ReviewEntity>> getReviewsByCustomer(@PathVariable int customerId) {
    //    return ResponseEntity.ok(reviewService.getReviewsByCustomer(customerId));
    //}

    // Update a review
    @PutMapping("/updateReview/{reviewid}")
    public ResponseEntity<ReviewEntity> updateReview(@PathVariable int id, @RequestBody ReviewEntity updatedReview) {
        ReviewEntity review = reviewService.updateReview(id, updatedReview);
        if (review != null) {
            return ResponseEntity.ok(review);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a review
    @DeleteMapping("/deleteReview/{reviewid}")
    public ResponseEntity<String> deleteReview(@PathVariable int id) {
        boolean deleted = reviewService.deleteReview(id);
        if (deleted) {
            return ResponseEntity.ok("Review deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
