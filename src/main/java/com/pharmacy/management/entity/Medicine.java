package com.pharmacy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Medicines", schema = "dbo")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MedicineId")
    private Integer medicineId;

    @NotBlank(message = "Medicine name is required.")
    @Size(max = 150, message = "Medicine name cannot be longer than 150 characters.")
    @Column(name = "MedicineName", nullable = false, length = 150)
    private String medicineName;

    @Size(max = 150, message = "Generic name cannot be longer than 150 characters.")
    @Column(name = "GenericName", length = 150)
    private String genericName;

    @Size(max = 100, message = "Category cannot be longer than 100 characters.")
    @Column(name = "Category", length = 100)
    private String category;

    @NotBlank(message = "Batch number is required.")
    @Size(max = 80, message = "Batch number cannot be longer than 80 characters.")
    @Column(name = "BatchNumber", nullable = false, length = 80)
    private String batchNumber;

    @NotNull(message = "Purchase price is required.")
    @DecimalMin(value = "0.00", message = "Purchase price cannot be negative.")
    @Column(name = "PurchasePrice", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @NotNull(message = "Selling price is required.")
    @DecimalMin(value = "0.00", message = "Selling price cannot be negative.")
    @Column(name = "SellingPrice", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull(message = "Stock quantity is required.")
    @Min(value = 0, message = "Stock quantity cannot be negative.")
    @Column(name = "QuantityInStock", nullable = false)
    private Integer quantityInStock = 0;

    @NotNull(message = "Reorder level is required.")
    @Min(value = 0, message = "Reorder level cannot be negative.")
    @Column(name = "ReorderLevel", nullable = false)
    private Integer reorderLevel = 0;

    @NotNull(message = "Expiry date is required.")
    @Future(message = "Expiry date must be a future date.")
    @Column(name = "ExpiryDate", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "RequiresPrescription", nullable = false)
    private boolean requiresPrescription = false;

    @Column(name = "CreatedByUserId")
    private Integer createdByUserId;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    public Integer getMedicineId() { return medicineId; }
    public void setMedicineId(Integer medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public Integer getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
