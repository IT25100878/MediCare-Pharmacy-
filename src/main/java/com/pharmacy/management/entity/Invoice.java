package com.pharmacy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "Invoices", schema = "dbo")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InvoiceId")
    private Integer invoiceId;

    @Column(name = "InvoiceNumber", nullable = false, length = 40)
    private String invoiceNumber;

    @Column(name = "OrderId", nullable = false)
    private Integer orderId;

    @Column(name = "InvoiceStatus", nullable = false, length = 20)
    private String invoiceStatus = "ISSUED";

    @Column(name = "GeneratedAt", insertable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "PrintedAt")
    private LocalDateTime printedAt;

    @Column(name = "VoidedAt")
    private LocalDateTime voidedAt;

    @Column(name = "Notes", length = 500)
    private String notes;

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public LocalDateTime getPrintedAt() { return printedAt; }
    public void setPrintedAt(LocalDateTime printedAt) { this.printedAt = printedAt; }
    public LocalDateTime getVoidedAt() { return voidedAt; }
    public void setVoidedAt(LocalDateTime voidedAt) { this.voidedAt = voidedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
