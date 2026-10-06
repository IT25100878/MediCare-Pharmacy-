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