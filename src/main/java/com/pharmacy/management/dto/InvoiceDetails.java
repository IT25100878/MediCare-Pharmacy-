package com.pharmacy.management.dto;

import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.entity.Invoice;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceDetails(Invoice invoice,
                             CustomerOrder order,
                             List<InvoiceLine> lines,
                             BigDecimal netAmount) {
}
