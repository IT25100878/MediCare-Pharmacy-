package com.pharmacy.management.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseOrderReceiptForm {

    @NotNull(message = "Choose a purchase order.")
    private Integer purchaseOrderId;

    @NotNull(message = "Choose a medicine.")
    private Integer medicineId;

    @NotNull(message = "Choose the receiving branch.")
    private Integer branchId;

    @NotBlank(message = "Batch number is required.")
    @Size(max = 80, message = "Batch number cannot exceed 80 characters.")
    private String batchNumber;

    @NotNull(message = "Received quantity is required.")
    @Min(value = 1, message = "Received quantity must be at least 1.")
    private Integer receivedQuantity;

    @NotNull(message = "Purchase price is required.")
    @DecimalMin(value = "0.00", message = "Purchase price cannot be negative.")
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @NotNull(message = "Selling price is required.")
    @DecimalMin(value = "0.00", message = "Selling price cannot be negative.")
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull(message = "Received date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate receivedDate = LocalDate.now();

    @NotNull(message = "Expiry date is required.")
    @Future(message = "Expiry date must be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;