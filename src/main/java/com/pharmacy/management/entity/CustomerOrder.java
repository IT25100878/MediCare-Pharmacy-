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
import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerOrders", schema = "dbo")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Integer orderId;

    @NotBlank(message = "Order number is required.")
    @Size(max = 30, message = "Order number cannot exceed 30 characters.")
    @Column(name = "OrderNumber", nullable = false, length = 30)
    private String orderNumber;

    @NotBlank(message = "Customer name is required.")
    @Size(max = 120, message = "Customer name cannot exceed 120 characters.")
    @Column(name = "CustomerName", nullable = false, length = 120)
    private String customerName;

    @NotBlank(message = "Customer phone is required.")
    @Size(max = 30, message = "Customer phone cannot exceed 30 characters.")
    @Column(name = "CustomerPhone", nullable = false, length = 30)
    private String customerPhone;

    @NotNull(message = "Order date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "OrderDate", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @NotNull(message = "Total amount is required.")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative.")
    @Column(name = "TotalAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull(message = "Discount amount is required.")
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative.")
    @Column(name = "DiscountAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotBlank(message = "Payment status is required.")
    @Pattern(regexp = "PENDING|PAID|REFUNDED", message = "Choose a valid payment status.")
    @Column(name = "PaymentStatus", nullable = false, length = 20)
    private String paymentStatus = "PENDING";

    @NotBlank(message = "Order status is required.")
    @Pattern(regexp = "NEW|PROCESSING|COMPLETED|CANCELLED", message = "Choose a valid order status.")
    @Column(name = "OrderStatus", nullable = false, length = 20)
    private String orderStatus = "NEW";

    @Column(name = "CashierUserId")
    private Integer cashierUserId;

    @Column(name = "BranchId")
    private Integer branchId;

    @Column(name = "PrescriptionId")
    private Integer prescriptionId;

    @Column(name = "PaymentMethod", length = 20)
    private String paymentMethod;

    @Column(name = "FulfilledAt")
    private LocalDateTime fulfilledAt;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getCashierUserId() {
        return cashierUserId;
    }

    public void setCashierUserId(Integer cashierUserId) {
        this.cashierUserId = cashierUserId;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Integer prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getFulfilledAt() {
        return fulfilledAt;
    }

    public void setFulfilledAt(LocalDateTime fulfilledAt) {
        this.fulfilledAt = fulfilledAt;
    }
}
