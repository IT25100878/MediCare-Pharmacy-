package com.pharmacy.management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockTransferForm {

    @NotNull(message = "Choose a batch to transfer.")
    private Integer batchId;

    @NotNull(message = "Choose the source branch.")
    private Integer fromBranchId;

    @NotNull(message = "Choose the destination branch.")
    private Integer toBranchId;

    @NotNull(message = "Enter the quantity to transfer.")
    @Min(value = 1, message = "Transfer quantity must be at least 1.")
    private Integer quantity;

    @Size(max = 500, message = "Notes cannot exceed 500 characters.")
    private String notes;

    public Integer getBatchId() { return batchId; }
    public void setBatchId(Integer batchId) { this.batchId = batchId; }
    public Integer getFromBranchId() { return fromBranchId; }
    public void setFromBranchId(Integer fromBranchId) { this.fromBranchId = fromBranchId; }
    public Integer getToBranchId() { return toBranchId; }
    public void setToBranchId(Integer toBranchId) { this.toBranchId = toBranchId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
