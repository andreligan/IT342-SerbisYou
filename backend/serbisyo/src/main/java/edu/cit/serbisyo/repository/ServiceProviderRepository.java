package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ServiceProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderEntity, Long> {
    // Find by business category
    List<ServiceProviderEntity> findByVerifiedTrue();
    Optional<ServiceProviderEntity> findByUserAuthUserName(String username);
    
    // Make sure the method is defined correctly
    Optional<ServiceProviderEntity> findByUserAuthUserId(Long userId);
}
