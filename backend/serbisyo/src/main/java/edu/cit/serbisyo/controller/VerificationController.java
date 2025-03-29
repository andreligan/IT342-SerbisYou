package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.VerificationEntity;
import edu.cit.serbisyo.service.VerificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    // CREATE a verification request
    @PostMapping
    public VerificationEntity createVerification(@RequestBody VerificationEntity verification) {
        return verificationService.createVerification(verification);
    }

    // READ all verification requests
    @GetMapping
    public List<VerificationEntity> getAllVerifications() {
        return verificationService.getAllVerifications();
    }

    // READ a specific verification request by ID
    @GetMapping("/{verificationId}")
    public VerificationEntity getVerification(@PathVariable Long verificationId) {
        return verificationService.getVerification(verificationId);
    }

    // UPDATE a verification request
    @PutMapping("/{verificationId}")
    public VerificationEntity updateVerification(
            @PathVariable Long verificationId,
            @RequestBody VerificationEntity updatedVerification) {
        return verificationService.updateVerification(verificationId, updatedVerification);
    }

    // DELETE a verification request
    @DeleteMapping("/{verificationId}")
    public String deleteVerification(@PathVariable Long verificationId) {
        return verificationService.deleteVerification(verificationId);
    }
}
