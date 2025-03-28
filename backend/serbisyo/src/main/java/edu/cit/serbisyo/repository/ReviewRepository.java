package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    // Find all reviews by a specific service provider
    List<ReviewEntity> findByServiceProviderId(Long serviceProviderId);

    // Find all reviews by a specific customer
    List<ReviewEntity> findByCustomerId(Long customerId);
}