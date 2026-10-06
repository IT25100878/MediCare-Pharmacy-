package com.pharmacy.management.service;

import com.pharmacy.management.dto.ApprovedPrescriptionOption;
import com.pharmacy.management.dto.CashierCheckoutForm;
import com.pharmacy.management.dto.PromotionDiscount;
import com.pharmacy.management.entity.BranchStock;
import com.pharmacy.management.entity.CheckoutOrderItem;
import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.entity.MedicineBatch;
import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.entity.Prescription;
import com.pharmacy.management.entity.StockTransaction;
import com.pharmacy.management.repository.BranchStockRepository;
import com.pharmacy.management.repository.CashierCheckoutLinkRepository;
import com.pharmacy.management.repository.CheckoutOrderItemRepository;
import com.pharmacy.management.repository.CustomerOrderRepository;
import com.pharmacy.management.repository.MedicineBatchRepository;
import com.pharmacy.management.repository.PharmacyBranchRepository;
import com.pharmacy.management.repository.PrescriptionDispenseRepository;
import com.pharmacy.management.repository.PrescriptionRepository;
import com.pharmacy.management.repository.StockTransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CashierCheckoutService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CheckoutOrderItemRepository checkoutOrderItemRepository;
    private final CashierCheckoutLinkRepository checkoutLinkRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDispenseRepository prescriptionDispenseRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final PharmacyBranchRepository pharmacyBranchRepository;
    private final BranchStockRepository branchStockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CurrentUserService currentUserService;
    private final MedicineService medicineService;
    private final PromotionService promotionService;
    private final InvoiceService invoiceService;

    public CashierCheckoutService(CustomerOrderRepository customerOrderRepository,
                                  CheckoutOrderItemRepository checkoutOrderItemRepository,
                                  CashierCheckoutLinkRepository checkoutLinkRepository,
                                  PrescriptionRepository prescriptionRepository,
                                  PrescriptionDispenseRepository prescriptionDispenseRepository,
                                  MedicineBatchRepository medicineBatchRepository,
                                  PharmacyBranchRepository pharmacyBranchRepository,
                                  BranchStockRepository branchStockRepository,
                                  StockTransactionRepository stockTransactionRepository,
                                  CurrentUserService currentUserService,
                                  MedicineService medicineService,
                                  PromotionService promotionService,
                                  InvoiceService invoiceService) {
        this.customerOrderRepository = customerOrderRepository;
        this.checkoutOrderItemRepository = checkoutOrderItemRepository;
        this.checkoutLinkRepository = checkoutLinkRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionDispenseRepository = prescriptionDispenseRepository;
        this.medicineBatchRepository = medicineBatchRepository;
        this.pharmacyBranchRepository = pharmacyBranchRepository;
        this.branchStockRepository = branchStockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.currentUserService = currentUserService;
        this.medicineService = medicineService;
        this.promotionService = promotionService;
        this.invoiceService = invoiceService;
    }

    public List<MedicineBatch> findSaleableBatches() {
        return medicineBatchRepository.findByActiveTrueOrderByExpiryDateAsc().stream()
                .filter(batch -> batch.getAvailableQuantity() != null && batch.getAvailableQuantity() > 0)
                .filter(batch -> batch.getExpiryDate() != null && batch.getExpiryDate().isAfter(LocalDate.now()))
                .toList();
    }

    public List<PharmacyBranch> findActiveBranches() {
        return pharmacyBranchRepository.findByActiveTrueOrderByBranchNameAsc();
    }

    public List<ApprovedPrescriptionOption> findApprovedPrescriptions() {
        return prescriptionDispenseRepository.findApprovedUndispensed();
    }

    public List<CustomerOrder> findRecentCompletedOrders() {
        return customerOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate")).stream()
                .filter(order -> "COMPLETED".equals(order.getOrderStatus()))
                .limit(12)
                .toList();
    }

    @Transactional
    public CustomerOrder checkout(CashierCheckoutForm form, String currentUserEmail) {
        MedicineBatch batch = requireSaleableBatch(form.getBatchId());
        PharmacyBranch branch = requireActiveBranch(form.getBranchId());
        BranchStock branchStock = requireBranchStock(branch, batch, form.getQuantity());
        Medicine medicine = batch.getMedicine();
        Prescription prescription = requirePrescriptionIfNeeded(form, medicine);
        Integer userId = currentUserService.getUserId(currentUserEmail);

        BigDecimal unitPrice = requireSellingPrice(batch);
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(form.getQuantity()));
        PromotionDiscount promotionDiscount = promotionService.calculateBestDiscount(medicine, branch, totalAmount);

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(createOrderNumber());
        order.setCustomerName(form.getCustomerName().trim());
        order.setCustomerPhone(form.getCustomerPhone().trim());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(promotionDiscount.discountAmount());
        order.setPaymentStatus("PAID");
        order.setOrderStatus("COMPLETED");
        order.setCashierUserId(userId);
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        checkoutLinkRepository.attachCheckout(savedOrder.getOrderId(), branch.getBranchId(),
                prescription == null ? null : prescription.getPrescriptionId(), form.getPaymentMethod());
        createOrderItem(savedOrder, batch, form.getQuantity(), unitPrice);
        releaseStock(branchStock, batch, savedOrder.getOrderId(), form.getQuantity(), userId);
        promotionService.recordRedemption(promotionDiscount, savedOrder.getOrderId());
        invoiceService.createForOrder(savedOrder);

        if (prescription != null) {
            prescriptionDispenseRepository.markDispensed(prescription.getPrescriptionId(), savedOrder.getOrderId());
        }

        medicineService.refreshMedicineStockSummary(medicine.getMedicineId());
        return savedOrder;
    }

    private Prescription requirePrescriptionIfNeeded(CashierCheckoutForm form, Medicine medicine) {
        if (!medicine.isRequiresPrescription()) {
            if (form.getPrescriptionId() != null) {
                throw new IllegalArgumentException("The selected medicine is OTC. Clear the prescription selection before checkout.");
            }
            return null;
        }
        if (form.getPrescriptionId() == null) {
            throw new IllegalArgumentException("This medicine requires an approved prescription.");
        }

        Prescription prescription = prescriptionRepository.findById(form.getPrescriptionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found."));
        if (!"APPROVED".equals(prescription.getPrescriptionStatus())) {
            throw new IllegalArgumentException("Only an approved prescription can be used for checkout.");
        }
        if (prescriptionDispenseRepository.isAlreadyDispensed(prescription.getPrescriptionId())) {
            throw new IllegalArgumentException("This prescription has already been dispensed.");
        }
        if (!sameCustomer(prescription, form)) {
            throw new IllegalArgumentException("Customer name and phone must match the approved prescription.");
        }
        return prescription;
    }

    private MedicineBatch requireSaleableBatch(Integer batchId) {
        MedicineBatch batch = medicineBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine batch not found."));
        if (!batch.isActive() || batch.getAvailableQuantity() == null || batch.getAvailableQuantity() <= 0) {
            throw new IllegalArgumentException("This medicine batch has no available stock.");
        }
        if (batch.getExpiryDate() == null || !batch.getExpiryDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Expired medicine cannot be sold.");
        }
        return batch;
    }

    private PharmacyBranch requireActiveBranch(Integer branchId) {
        PharmacyBranch branch = pharmacyBranchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found."));
        if (!branch.isActive()) {
            throw new IllegalArgumentException("Choose an active branch.");
        }
        return branch;
    }

    private BranchStock requireBranchStock(PharmacyBranch branch, MedicineBatch batch, Integer quantity) {
        BranchStock stock = branchStockRepository
                .findByBranch_BranchIdAndBatch_BatchId(branch.getBranchId(), batch.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException("This batch is not available at the selected branch."));
        if (quantity == null || stock.getQuantityInStock() == null || stock.getQuantityInStock() < quantity) {
            throw new IllegalArgumentException("The selected branch does not have enough stock for this sale.");
        }
        return stock;
    }

    private BigDecimal requireSellingPrice(MedicineBatch batch) {
        if (batch.getSellingPrice() == null || batch.getSellingPrice().signum() < 0) {
            throw new IllegalArgumentException("This batch does not have a valid selling price.");
        }
        return batch.getSellingPrice();
    }

    private void createOrderItem(CustomerOrder order, MedicineBatch batch, Integer quantity, BigDecimal unitPrice) {
        CheckoutOrderItem item = new CheckoutOrderItem();
        item.setOrderId(order.getOrderId());
        item.setMedicineId(batch.getMedicine().getMedicineId());
        item.setBatchId(batch.getBatchId());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        checkoutOrderItemRepository.save(item);
    }

    private void releaseStock(BranchStock branchStock,
                              MedicineBatch batch,
                              Integer orderId,
                              Integer quantity,
                              Integer userId) {
        branchStock.setQuantityInStock(branchStock.getQuantityInStock() - quantity);
        branchStock.setUpdatedAt(LocalDateTime.now());
        branchStockRepository.save(branchStock);

        batch.setAvailableQuantity(batch.getAvailableQuantity() - quantity);
        medicineBatchRepository.save(batch);

        StockTransaction transaction = new StockTransaction();
        transaction.setBatch(batch);
        transaction.setBranch(branchStock.getBranch());
        transaction.setTransactionType("SALE_OUT");
        transaction.setQuantityChange(-quantity);
        transaction.setReferenceType("CUSTOMER_ORDER");
        transaction.setReferenceId(orderId);
        transaction.setNotes("Cashier checkout completed.");
        transaction.setCreatedByUserId(userId);
        stockTransactionRepository.save(transaction);
    }

    private boolean sameCustomer(Prescription prescription, CashierCheckoutForm form) {
        String prescriptionName = normalizeName(prescription.getCustomerName());
        String formName = normalizeName(form.getCustomerName());
        String prescriptionPhone = normalizePhone(prescription.getCustomerPhone());
        String formPhone = normalizePhone(form.getCustomerPhone());
        return prescriptionName.equals(formName) && prescriptionPhone.equals(formPhone);
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String createOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return "ORD-" + date + "-" + random;
    }
}
