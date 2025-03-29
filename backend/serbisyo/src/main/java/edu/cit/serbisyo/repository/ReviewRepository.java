package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    // Correct query method
    List<ReviewEntity> findByProviderProviderId(long providerId);
}