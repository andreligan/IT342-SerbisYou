package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByProviderProviderId(Long providerId);
    List<ReviewEntity> findByBookingBookingId(Long bookingId);
    
    // Custom query to get reviews for a specific service
    @Query("SELECT r FROM ReviewEntity r WHERE r.booking.service.serviceId = :serviceId")
    List<ReviewEntity> findByServiceId(@Param("serviceId") Long serviceId);
    
    // Custom query to get average rating for a service
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.booking.service.serviceId = :serviceId")
    Double getAverageRatingForService(@Param("serviceId") Long serviceId);
    
    // Custom query to count reviews for a service
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.booking.service.serviceId = :serviceId")
    Long countReviewsForService(@Param("serviceId") Long serviceId);
    
    boolean existsByCustomerCustomerIdAndBookingBookingId(Long customerId, Long bookingId);
}