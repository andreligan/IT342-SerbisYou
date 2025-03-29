package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ServiceProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderEntity, Long> {
    // Find by business category
    List<ServiceProviderEntity> findByVerifiedTrue();
}
