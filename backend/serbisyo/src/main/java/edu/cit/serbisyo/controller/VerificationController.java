package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.VerificationEntity;
import edu.cit.serbisyo.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    // Create a verification request
    @PostMapping
    public ResponseEntity<VerificationEntity> createVerification(@RequestBody VerificationEntity verification) {
        return ResponseEntity.ok(verificationService.createVerification(verification));
    }

    // Get all verifications
    @GetMapping
    public ResponseEntity<List<VerificationEntity>> getAllVerifications() {
        return ResponseEntity.ok(verificationService.getAllVerifications());
    }

    // Get a verification by ID
    @GetMapping("/{id}")
    public ResponseEntity<VerificationEntity> getVerificationById(@PathVariable int id) {
        Optional<VerificationEntity> verification = verificationService.getVerificationById(id);
        return verification.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get verifications by service provider ID
    @GetMapping("/service-provider/{serviceProviderId}")
    public ResponseEntity<List<VerificationEntity>> getVerificationsByServiceProviderId(@PathVariable int serviceProviderId) {
        return ResponseEntity.ok(verificationService.getVerificationsByServiceProviderId(serviceProviderId));
    }

    // Get verifications by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<VerificationEntity>> getVerificationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(verificationService.getVerificationsByStatus(status));
    }

    // Update a verification
    @PutMapping("/{id}")
    public ResponseEntity<VerificationEntity> updateVerification(@PathVariable int id, @RequestBody VerificationEntity updatedVerification) {
        VerificationEntity verification = verificationService.updateVerification(id, updatedVerification);
        if (verification != null) {
            return ResponseEntity.ok(verification);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete a verification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerification(@PathVariable int id) {
        if (verificationService.deleteVerification(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
