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

import java.time.LocalDateTime;

@Entity
@Table(name = "StockTransactions", schema = "dbo")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StockTransactionId")
    private Integer stockTransactionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BatchId", nullable = false)
    private MedicineBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BranchId", nullable = false)
    private PharmacyBranch branch;

    @Column(name = "TransactionType", nullable = false, length = 30)
    private String transactionType;

    @Column(name = "QuantityChange", nullable = false)
    private Integer quantityChange;

    @Column(name = "ReferenceType", length = 40)
    private String referenceType;

    @Column(name = "ReferenceId")
    private Integer referenceId;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "CreatedByUserId")
    private Integer createdByUserId;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getStockTransactionId() { return stockTransactionId; }
    public void setStockTransactionId(Integer stockTransactionId) { this.stockTransactionId = stockTransactionId; }
    public MedicineBatch getBatch() { return batch; }
    public void setBatch(MedicineBatch batch) { this.batch = batch; }
    public PharmacyBranch getBranch() { return branch; }
    public void setBranch(PharmacyBranch branch) { this.branch = branch; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Integer getQuantityChange() { return quantityChange; }
    public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Integer getReferenceId() { return referenceId; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

