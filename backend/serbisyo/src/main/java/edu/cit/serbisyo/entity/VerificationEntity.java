package edu.cit.serbisyo.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Verification")
public class VerificationEntity {
    @Id
    private Long verificationId;

    @OneToOne
    @JoinColumn(name = "providerId")
    private ServiceProviderEntity serviceProvider;

    private Long reviewedByAdmin;
    private String idProof;
    private String businessPermit;
    private String professionalLicense;
    private String verificationLevel;
    private String reviewNotes;
    private Date verifiedAt;
    private Date rejectedAt;
    private String status;

    // Getters and Setters

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }

    public ServiceProviderEntity getProvider() {
        return serviceProvider;
    }

    public void setProvider(ServiceProviderEntity provider) {
        this.serviceProvider = provider;
    }

    public Long getReviewedByAdmin() {
        return reviewedByAdmin;
    }

    public void setReviewedByAdmin(Long reviewedByAdmin) {
        this.reviewedByAdmin = reviewedByAdmin;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    public String getBusinessPermit() {
        return businessPermit;
    }

    public void setBusinessPermit(String businessPermit) {
        this.businessPermit = businessPermit;
    }

    public String getProfessionalLicense() {
        return professionalLicense;
    }

    public void setProfessionalLicense(String professionalLicense) {
        this.professionalLicense = professionalLicense;
    }

    public String getVerificationLevel() {
        return verificationLevel;
    }

    public void setVerificationLevel(String verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Date getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Date rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}
