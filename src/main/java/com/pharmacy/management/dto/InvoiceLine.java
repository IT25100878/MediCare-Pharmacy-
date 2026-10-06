package com.pharmacy.management.dto;

import java.math.BigDecimal;

public record InvoiceLine(String medicineName,
                          String batchNumber,
                          Integer quantity,
                          BigDecimal unitPrice,
                          BigDecimal lineTotal) {
}
