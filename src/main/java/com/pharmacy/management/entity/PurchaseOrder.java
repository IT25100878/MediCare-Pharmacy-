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

    @NotNull(message = "Total amount is required.")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative.")
    @Column(name = "TotalAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotBlank(message = "Purchase order status is required.")
    @Pattern(regexp = "PENDING|ORDERED|RECEIVED|CANCELLED", message = "Choose a valid purchase order status.")
    @Column(name = "PurchaseOrderStatus", nullable = false, length = 20)
    private String purchaseOrderStatus = "PENDING";

    @Column(name = "ProcurementUserId")
    private Integer procurementUserId;

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPurchaseOrderStatus() {
        return purchaseOrderStatus;
    }

    public void setPurchaseOrderStatus(String purchaseOrderStatus) {
        this.purchaseOrderStatus = purchaseOrderStatus;
    }

    public Integer getProcurementUserId() {
        return procurementUserId;
    }

    public void setProcurementUserId(Integer procurementUserId) {
        this.procurementUserId = procurementUserId;
    }
}


