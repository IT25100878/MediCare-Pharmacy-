package com.pharmacy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Promotions", schema = "dbo")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionId")
    private Integer promotionId;

    @NotBlank(message = "Campaign code is required.")
    @Size(max = 40, message = "Campaign code cannot exceed 40 characters.")
    @Column(name = "CampaignCode", nullable = false, length = 40)
    private String campaignCode;

    @NotBlank(message = "Campaign name is required.")
    @Size(max = 150, message = "Campaign name cannot exceed 150 characters.")
    @Column(name = "CampaignName", nullable = false, length = 150)
    private String campaignName;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    @Column(name = "Description", length = 500)
    private String description;

    @NotBlank(message = "Discount type is required.")
    @Pattern(regexp = "PERCENT|FIXED", message = "Choose a valid discount type.")
    @Column(name = "DiscountType", nullable = false, length = 20)
    private String discountType = "PERCENT";

    @NotNull(message = "Discount value is required.")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than zero.")
    @Column(name = "DiscountValue", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @NotNull(message = "Minimum order amount is required.")
    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative.")
    @Column(name = "MinimumOrderAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "MedicineId")
    private Integer medicineId;

    @Size(max = 100, message = "Category cannot exceed 100 characters.")
    @Column(name = "Category", length = 100)
    private String category;

    @Column(name = "BranchId")
    private Integer branchId;

    @NotNull(message = "Start date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "EndDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "IsActive", nullable = false)
    private boolean active = true;

    @Column(name = "IsArchived", nullable = false)
    private boolean archived = false;

    @Column(name = "CreatedByUserId")
    private Integer createdByUserId;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    public Integer getPromotionId() { return promotionId; }
    public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }
    public String getCampaignCode() { return campaignCode; }
    public void setCampaignCode(String campaignCode) { this.campaignCode = campaignCode; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }
    public Integer getMedicineId() { return medicineId; }
    public void setMedicineId(Integer medicineId) { this.medicineId = medicineId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

