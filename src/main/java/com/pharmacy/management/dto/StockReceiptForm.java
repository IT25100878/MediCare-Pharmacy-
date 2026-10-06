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

public class StockReceiptForm {

    @NotNull(message = "Choose a medicine.")
    private Integer medicineId;

    @NotNull(message = "Choose the branch receiving this stock.")
    private Integer branchId;

    @NotBlank(message = "Batch number is required.")
    @Size(max = 80, message = "Batch number cannot exceed 80 characters.")
    private String batchNumber;

    @NotNull(message = "Purchase price is required.")
    @DecimalMin(value = "0.00", message = "Purchase price cannot be negative.")
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is required.")
    @DecimalMin(value = "0.00", message = "Selling price cannot be negative.")
    private BigDecimal sellingPrice;
