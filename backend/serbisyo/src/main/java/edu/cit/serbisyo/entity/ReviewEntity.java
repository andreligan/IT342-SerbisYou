package edu.cit.serbisyo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "Review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "customerId")
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "providerId")
    private ServiceProviderEntity provider;

    private int rating;
    private String comment;
    private Date reviewDate;

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private BookingEntity booking;

    // Getters and Setters

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public ServiceProviderEntity getProvider() {
        return provider;
    }

    public void setProvider(ServiceProviderEntity provider) {
        this.provider = provider;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public BookingEntity getBooking() {
        return booking;
    }

    public void setBooking(BookingEntity booking) {
        this.booking = booking;
    }
}