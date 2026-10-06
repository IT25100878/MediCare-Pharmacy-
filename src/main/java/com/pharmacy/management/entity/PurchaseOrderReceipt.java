package com.pharmacy.management.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PurchaseOrderReceipts", schema = "dbo")
public class PurchaseOrderReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PurchaseOrderReceiptId")
    private Integer purchaseOrderReceiptId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PurchaseOrderId", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MedicineId", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BatchId", nullable = false)
    private MedicineBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BranchId", nullable = false)
    private PharmacyBranch branch;

    @Column(name = "BatchNumber", nullable = false, length = 80)
    private String batchNumber;

    @Column(name = "ReceivedQuantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "PurchasePrice", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "SellingPrice", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "ReceivedDate", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "ExpiryDate", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "ReceivedByUserId")
    private Integer receivedByUserId;

    @Column(name = "ReceivedAt", insertable = false, updatable = false)
    private LocalDateTime receivedAt;