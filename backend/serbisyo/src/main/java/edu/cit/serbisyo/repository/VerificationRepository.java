package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.VerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationEntity, Integer> {

    // Find verifications by status
    List<VerificationEntity> findByStatus(String status);

    // Find verification records by service provider ID
    List<VerificationEntity> findByServiceProviderId(int serviceProviderId);
}
