package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import edu.cit.serbisyo.entity.ReviewEntity;
import edu.cit.serbisyo.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public ReviewEntity createReview(@RequestBody ReviewEntity review) {
        return reviewService.createReview(review);
    }

    @GetMapping("/getByProvider/{providerId}")
    public List<ReviewEntity> getReviewsByProvider(@PathVariable Long providerId) {
        return reviewService.getReviewsByProvider(providerId);
    }

    @PutMapping("/update/{reviewId}")
    public ReviewEntity updateReview(@PathVariable Long reviewId, @RequestBody ReviewEntity updatedReview) {
        return reviewService.updateReview(reviewId, updatedReview);
    }

    @DeleteMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId);
    }
}
