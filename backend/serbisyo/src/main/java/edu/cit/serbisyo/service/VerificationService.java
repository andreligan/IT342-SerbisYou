package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.VerificationEntity;
import edu.cit.serbisyo.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VerificationService {

    @Autowired
    private VerificationRepository verificationRepository;

    // Create a new verification request
    public VerificationEntity createVerification(VerificationEntity verification) {
        return verificationRepository.save(verification);
    }

    // Get all verifications
    public List<VerificationEntity> getAllVerifications() {
        return verificationRepository.findAll();
    }

    // Get a verification by ID
    public Optional<VerificationEntity> getVerificationById(int id) {
        return verificationRepository.findById(id);
    }

    // Get verifications by service provider ID
    public List<VerificationEntity> getVerificationsByServiceProviderId(int serviceProviderId) {
        return verificationRepository.findByServiceProviderId(serviceProviderId);
    }

    // Get verifications by status
    public List<VerificationEntity> getVerificationsByStatus(String status) {
        return verificationRepository.findByStatus(status);
    }

    // Update a verification request
    public VerificationEntity updateVerification(int id, VerificationEntity updatedVerification) {
        return verificationRepository.findById(id)
                .map(verification -> {
                    verification.setStatus(updatedVerification.getStatus());
                    verification.setRemarks(updatedVerification.getRemarks());
                    return verificationRepository.save(verification);
                })
                .orElse(null);
    }

    // Delete a verification request
    public boolean deleteVerification(int id) {
        if (verificationRepository.existsById(id)) {
            verificationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
