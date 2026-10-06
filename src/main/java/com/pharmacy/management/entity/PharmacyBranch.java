package com.pharmacy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "PharmacyBranches", schema = "dbo")
public class PharmacyBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BranchId")
    private Integer branchId;

    @NotBlank(message = "Branch name is required.")
    @Size(max = 150, message = "Branch name cannot exceed 150 characters.")
    @Column(name = "BranchName", nullable = false, length = 150)
    private String branchName;

    @NotBlank(message = "Address is required.")
    @Size(max = 300, message = "Address cannot exceed 300 characters.")
    @Column(name = "Address", nullable = false, length = 300)
    private String address;

    @NotBlank(message = "Contact number is required.")
    @Size(max = 30, message = "Contact number cannot exceed 30 characters.")
    @Column(name = "ContactNumber", nullable = false, length = 30)
    private String contactNumber;

    @Size(max = 120, message = "Manager name cannot exceed 120 characters.")
    @Column(name = "ManagerName", length = 120)
    private String managerName;

    @Column(name = "IsActive", nullable = false)
    private boolean active = true;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
