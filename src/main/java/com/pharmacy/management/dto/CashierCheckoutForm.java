package com.pharmacy.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CashierCheckoutForm {

    @NotBlank(message = "Customer name is required.")
    @Size(max = 120, message = "Customer name cannot exceed 120 characters.")
    private String customerName;

    @NotBlank(message = "Customer phone is required.")
    @Size(max = 30, message = "Customer phone cannot exceed 30 characters.")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,30}$", message = "Enter a valid phone number.")
    private String customerPhone;

    @NotNull(message = "Choose a branch.")
    private Integer branchId;

    @NotNull(message = "Choose a medicine batch.")
    private Integer batchId;

    @NotNull(message = "Enter the quantity.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer quantity;

    @NotBlank(message = "Choose a payment method.")
    @Pattern(regexp = "CASH|CARD|ONLINE", message = "Choose a valid payment method.")
    private String paymentMethod = "CASH";

    private Integer prescriptionId;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public Integer getBatchId() { return batchId; }
    public void setBatchId(Integer batchId) { this.batchId = batchId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Integer getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Integer prescriptionId) { this.prescriptionId = prescriptionId; }
}
