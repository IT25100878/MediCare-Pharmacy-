package com.pharmacy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "Prescriptions", schema = "dbo")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PrescriptionId")
    private Integer prescriptionId;

    @NotBlank(message = "Prescription number is required.")
    @Size(max = 30, message = "Prescription number cannot exceed 30 characters.")
    @Column(name = "PrescriptionNumber", nullable = false, length = 30)
    private String prescriptionNumber;

    @NotBlank(message = "Customer name is required.")
    @Size(max = 120, message = "Customer name cannot exceed 120 characters.")
    @Column(name = "CustomerName", nullable = false, length = 120)
    private String customerName;

    @NotBlank(message = "Customer phone is required.")
    @Size(max = 30, message = "Customer phone cannot exceed 30 characters.")
    @Column(name = "CustomerPhone", nullable = false, length = 30)
    private String customerPhone;

    @Size(max = 500, message = "File path cannot exceed 500 characters.")
    @Column(name = "PrescriptionFilePath", length = 500)
    private String prescriptionFilePath;

    @NotBlank(message = "Prescription status is required.")
    @Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "Choose a valid prescription status.")
    @Column(name = "PrescriptionStatus", nullable = false, length = 20)
    private String prescriptionStatus = "PENDING";

    @Column(name = "PharmacistUserId")
    private Integer pharmacistUserId;

    @Column(name = "IsArchived", nullable = false)
    private boolean archived = false;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;

    @Size(max = 500, message = "Pharmacist note cannot exceed 500 characters.")
    @Column(name = "PharmacistNote", length = 500)
    private String pharmacistNote;

    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Integer prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPrescriptionNumber() {
        return prescriptionNumber;
    }

    public void setPrescriptionNumber(String prescriptionNumber) {
        this.prescriptionNumber = prescriptionNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPrescriptionFilePath() {
        return prescriptionFilePath;
    }

    public void setPrescriptionFilePath(String prescriptionFilePath) {
        this.prescriptionFilePath = prescriptionFilePath;
    }

    public String getPrescriptionStatus() {
        return prescriptionStatus;
    }

    public void setPrescriptionStatus(String prescriptionStatus) {
        this.prescriptionStatus = prescriptionStatus;
    }

    public Integer getPharmacistUserId() {
        return pharmacistUserId;
    }

    public void setPharmacistUserId(Integer pharmacistUserId) {
        this.pharmacistUserId = pharmacistUserId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getPharmacistNote() {
        return pharmacistNote;
    }

    public void setPharmacistNote(String pharmacistNote) {
        this.pharmacistNote = pharmacistNote;
    }
}
