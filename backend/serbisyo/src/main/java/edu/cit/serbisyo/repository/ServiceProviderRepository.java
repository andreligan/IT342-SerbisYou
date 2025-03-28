package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ServiceProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderEntity, Long> {
    // Find by business category
    List<ServiceProviderEntity> findByCategory(String category);

    // Find all verified service providers
    List<ServiceProviderEntity> findByIsVerifiedTrue();

    // Find service providers by user ID
    List<ServiceProviderEntity> findByUserId(long userId);
}
