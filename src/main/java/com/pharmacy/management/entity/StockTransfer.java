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
@Table(name = "StockTransfers", schema = "dbo")
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StockTransferId")
    private Integer stockTransferId;

    @Column(name = "TransferNumber", nullable = false, length = 35)
    private String transferNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BatchId", nullable = false)
    private MedicineBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FromBranchId", nullable = false)
    private PharmacyBranch fromBranch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ToBranchId", nullable = false)
    private PharmacyBranch toBranch;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "TransferStatus", nullable = false, length = 20)
    private String transferStatus = "PENDING";

    @Column(name = "RequestedByUserId")
    private Integer requestedByUserId;

    @Column(name = "DispatchedByUserId")
    private Integer dispatchedByUserId;

    @Column(name = "ReceivedByUserId")
    private Integer receivedByUserId;

    @Column(name = "RequestedAt", insertable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "DispatchedAt")
    private LocalDateTime dispatchedAt;

    @Column(name = "ReceivedAt")
    private LocalDateTime receivedAt;

    @Column(name = "Notes", length = 500)
    private String notes;

    public Integer getStockTransferId() { return stockTransferId; }
    public void setStockTransferId(Integer stockTransferId) { this.stockTransferId = stockTransferId; }
    public String getTransferNumber() { return transferNumber; }
    public void setTransferNumber(String transferNumber) { this.transferNumber = transferNumber; }
    public MedicineBatch getBatch() { return batch; }
    public void setBatch(MedicineBatch batch) { this.batch = batch; }
    public PharmacyBranch getFromBranch() { return fromBranch; }
    public void setFromBranch(PharmacyBranch fromBranch) { this.fromBranch = fromBranch; }
    public PharmacyBranch getToBranch() { return toBranch; }
    public void setToBranch(PharmacyBranch toBranch) { this.toBranch = toBranch; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getTransferStatus() { return transferStatus; }
    public void setTransferStatus(String transferStatus) { this.transferStatus = transferStatus; }
    public Integer getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(Integer requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public Integer getDispatchedByUserId() { return dispatchedByUserId; }
    public void setDispatchedByUserId(Integer dispatchedByUserId) { this.dispatchedByUserId = dispatchedByUserId; }
    public Integer getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(Integer receivedByUserId) { this.receivedByUserId = receivedByUserId; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

