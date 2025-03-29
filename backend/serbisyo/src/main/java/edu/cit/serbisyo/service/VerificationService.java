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

    public VerificationEntity createVerification(VerificationEntity verification) {
        return verificationRepository.save(verification);
    }

    public List<VerificationEntity> getAllVerifications() {
        return verificationRepository.findAll();
    }

    public VerificationEntity getVerification(Long verificationId) {
        VerificationEntity verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification request not found"));
        return verification;
    }

    public VerificationEntity updateVerification(Long verificationId, VerificationEntity updatedVerification) {
        VerificationEntity existingVerification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification request not found"));
        existingVerification.setVerificationLevel(updatedVerification.getVerificationLevel());
        existingVerification.setReviewNotes(updatedVerification.getReviewNotes());
        return verificationRepository.save(existingVerification);
    }

    public String deleteVerification(Long verificationId) {
        if (verificationRepository.existsById(verificationId)) {
            verificationRepository.deleteById(verificationId);
            return "Verification request successfully deleted.";
        }
        return "Verification request not found.";
    }
}