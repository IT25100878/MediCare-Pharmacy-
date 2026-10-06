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
@Table(name = "BranchStock", schema = "dbo")
public class BranchStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BranchStockId")
    private Integer branchStockId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BranchId", nullable = false)
    private PharmacyBranch branch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BatchId", nullable = false)
    private MedicineBatch batch;

    @Column(name = "QuantityInStock", nullable = false)
    private Integer quantityInStock = 0;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public Integer getBranchStockId() { return branchStockId; }
    public void setBranchStockId(Integer branchStockId) { this.branchStockId = branchStockId; }
    public PharmacyBranch getBranch() { return branch; }
    public void setBranch(PharmacyBranch branch) { this.branch = branch; }
    public MedicineBatch getBatch() { return batch; }
    public void setBatch(MedicineBatch batch) { this.batch = batch; }
    public Integer getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
