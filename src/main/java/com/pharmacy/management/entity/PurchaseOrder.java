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

@Entity
@Table(name = "PurchaseOrders", schema = "dbo")
public class PurchaseOrder {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PurchaseOrderId")
    private Integer purchaseOrderId;

    @NotBlank(message = "Purchase order number is required.")
    @Size(max = 30, message = "Purchase order number cannot exceed 30 characters.")
    @Column(name = "PurchaseOrderNumber", nullable = false, length = 30)
    private String purchaseOrderNumber;

    @NotNull(message = "Please choose a supplier.")
    @Column(name = "SupplierId")
    private Integer supplierId;

    @NotNull(message = "Order date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "OrderDate", nullable = false)
    private LocalDate orderDate = LocalDate.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "ExpectedDate")
    private LocalDate expectedDate;
}
