package com.pharmacy.management.service;

import com.pharmacy.management.dto.InvoiceDetails;
import com.pharmacy.management.dto.InvoiceLine;
import com.pharmacy.management.entity.CheckoutOrderItem;
import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.entity.Invoice;
import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.entity.MedicineBatch;
import com.pharmacy.management.repository.CheckoutOrderItemRepository;
import com.pharmacy.management.repository.CustomerOrderRepository;
import com.pharmacy.management.repository.InvoiceRepository;
import com.pharmacy.management.repository.MedicineBatchRepository;
import com.pharmacy.management.repository.MedicineRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CheckoutOrderItemRepository checkoutOrderItemRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerOrderRepository customerOrderRepository,
                          CheckoutOrderItemRepository checkoutOrderItemRepository,
                          MedicineRepository medicineRepository,
                          MedicineBatchRepository medicineBatchRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.checkoutOrderItemRepository = checkoutOrderItemRepository;
        this.medicineRepository = medicineRepository;
        this.medicineBatchRepository = medicineBatchRepository;
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "generatedAt"));
    }

    public Invoice findById(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice was not found."));
    }

    public Invoice findByOrderId(Integer orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice was not found for this order."));
    }

    @Transactional
    public Invoice createForOrder(CustomerOrder order) {
        return invoiceRepository.findByOrderId(order.getOrderId()).orElseGet(() -> {
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(createInvoiceNumber());
            invoice.setOrderId(order.getOrderId());
            invoice.setInvoiceStatus("ISSUED");
            invoice.setNotes("Generated automatically from cashier checkout.");
            return invoiceRepository.save(invoice);
        });
    }

    public InvoiceDetails getDetails(Integer invoiceId) {
        Invoice invoice = findById(invoiceId);
        CustomerOrder order = customerOrderRepository.findById(invoice.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer order was not found."));
        List<CheckoutOrderItem> items = checkoutOrderItemRepository.findByOrderId(order.getOrderId());
        Map<Integer, Medicine> medicines = new HashMap<>();
        medicineRepository.findAllById(items.stream().map(CheckoutOrderItem::getMedicineId).toList())
                .forEach(medicine -> medicines.put(medicine.getMedicineId(), medicine));
        Map<Integer, MedicineBatch> batches = new HashMap<>();
        medicineBatchRepository.findAllById(items.stream()
                        .map(CheckoutOrderItem::getBatchId)
                        .filter(batchId -> batchId != null)
                        .toList())
                .forEach(batch -> batches.put(batch.getBatchId(), batch));

        List<InvoiceLine> lines = items.stream().map(item -> {
            Medicine medicine = medicines.get(item.getMedicineId());
            MedicineBatch batch = batches.get(item.getBatchId());
            BigDecimal unitPrice = safeMoney(item.getUnitPrice());
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            return new InvoiceLine(
                    medicine == null ? "Medicine record" : medicine.getMedicineName(),
                    batch == null ? "—" : batch.getBatchNumber(),
                    quantity,
                    unitPrice,
                    unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP)
            );
        }).toList();

        BigDecimal netAmount = safeMoney(order.getTotalAmount()).subtract(safeMoney(order.getDiscountAmount()))
                .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new InvoiceDetails(invoice, order, lines, netAmount);
    }

    @Transactional
    public void markPrinted(Integer invoiceId) {
        Invoice invoice = findById(invoiceId);
        if ("VOID".equals(invoice.getInvoiceStatus())) {
            throw new IllegalArgumentException("A void invoice cannot be printed.");
        }
        invoice.setPrintedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void voidInvoice(Integer invoiceId, String note) {
        Invoice invoice = findById(invoiceId);
        if ("VOID".equals(invoice.getInvoiceStatus())) {
            return;
        }
        invoice.setInvoiceStatus("VOID");
        invoice.setVoidedAt(LocalDateTime.now());
        invoice.setNotes(note == null || note.isBlank() ? "Voided by cashier/admin." : note.trim());
        invoiceRepository.save(invoice);
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String createInvoiceNumber() {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 7).toUpperCase(Locale.ROOT);
        return "INV-" + day + "-" + suffix;
    }
}
