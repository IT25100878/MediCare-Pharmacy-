package com.pharmacy.management.service;


import com.pharmacy.management.dto.PurchaseOrderReceiptForm;
import com.pharmacy.management.entity.BranchStock;
import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.entity.MedicineBatch;
import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.entity.PurchaseOrder;
import com.pharmacy.management.entity.PurchaseOrderReceipt;
import com.pharmacy.management.entity.StockTransaction;
import com.pharmacy.management.repository.BranchStockRepository;
import com.pharmacy.management.repository.MedicineBatchRepository;
import com.pharmacy.management.repository.MedicineRepository;
import com.pharmacy.management.repository.PharmacyBranchRepository;
import com.pharmacy.management.repository.PurchaseOrderReceiptRepository;
import com.pharmacy.management.repository.PurchaseOrderRepository;
import com.pharmacy.management.repository.StockTransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcurementReceiptService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderReceiptRepository receiptRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final PharmacyBranchRepository pharmacyBranchRepository;
    private final BranchStockRepository branchStockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final CurrentUserService currentUserService;
    private final MedicineService medicineService;


    public ProcurementReceiptService(PurchaseOrderRepository purchaseOrderRepository,
                                     PurchaseOrderReceiptRepository receiptRepository,
                                     MedicineRepository medicineRepository,
                                     MedicineBatchRepository medicineBatchRepository,
                                     PharmacyBranchRepository pharmacyBranchRepository,
                                     BranchStockRepository branchStockRepository,
                                     StockTransactionRepository stockTransactionRepository,
                                     CurrentUserService currentUserService,
                                     MedicineService medicineService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.receiptRepository = receiptRepository;
        this.medicineRepository = medicineRepository;
        this.medicineBatchRepository = medicineBatchRepository;
        this.pharmacyBranchRepository = pharmacyBranchRepository;
        this.branchStockRepository = branchStockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.currentUserService = currentUserService;
        this.medicineService = medicineService;
    }

    public List<PurchaseOrder> findReceivablePurchaseOrders() {
        return purchaseOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate")).stream()
                .filter(order -> !"RECEIVED".equals(order.getPurchaseOrderStatus()))
                .filter(order -> !"CANCELLED".equals(order.getPurchaseOrderStatus()))
                .filter(order -> !receiptRepository.existsByPurchaseOrder_PurchaseOrderId(order.getPurchaseOrderId()))
                .toList();
    }

    public List<PurchaseOrderReceipt> findAllReceipts() {
        return receiptRepository.findAllByOrderByReceivedAtDesc();
    }

    @Transactional
    public void receivePurchaseOrder(PurchaseOrderReceiptForm form, String currentUserEmail) {
        validateFormValues(form);

        PurchaseOrder purchaseOrder = requirePurchaseOrder(form.getPurchaseOrderId());
        validateReceivablePurchaseOrder(purchaseOrder);
        if (receiptRepository.existsByPurchaseOrder_PurchaseOrderId(purchaseOrder.getPurchaseOrderId())) {
            throw new IllegalArgumentException("This purchase order has already been received.");
        }

        Medicine medicine = requireMedicine(form.getMedicineId());
        PharmacyBranch branch = requireActiveBranch(form.getBranchId());
        Integer userId = currentUserService.getUserId(currentUserEmail);

        MedicineBatch batch = medicineBatchRepository
                .findByMedicine_MedicineIdAndBatchNumberIgnoreCase(medicine.getMedicineId(), form.getBatchNumber().trim())
                .orElseGet(() -> createBatch(form, medicine, userId));

        ensureCompatibleBatch(batch, form);
        batch.setInitialQuantity(batch.getInitialQuantity() + form.getReceivedQuantity());
        batch.setAvailableQuantity(batch.getAvailableQuantity() + form.getReceivedQuantity());
        batch.setPurchasePrice(form.getPurchasePrice());
        batch.setSellingPrice(form.getSellingPrice());
        batch.setReceivedDate(form.getReceivedDate());
        batch.setExpiryDate(form.getExpiryDate());
        batch = medicineBatchRepository.save(batch);

        final MedicineBatch receivedBatch = batch;
        BranchStock branchStock = branchStockRepository
                .findByBranch_BranchIdAndBatch_BatchId(branch.getBranchId(), receivedBatch.getBatchId())
                .orElseGet(() -> createEmptyBranchStock(branch, receivedBatch));
        branchStock.setQuantityInStock(branchStock.getQuantityInStock() + form.getReceivedQuantity());
        branchStock.setUpdatedAt(LocalDateTime.now());
        branchStockRepository.save(branchStock);

        medicine.setPurchasePrice(form.getPurchasePrice());
        medicine.setSellingPrice(form.getSellingPrice());
        medicine.setUpdatedAt(LocalDateTime.now());
        medicineRepository.save(medicine);

        createStockTransaction(receivedBatch, branch, form, purchaseOrder.getPurchaseOrderId(), userId);
        createReceipt(purchaseOrder, medicine, receivedBatch, branch, form, userId);

        purchaseOrder.setPurchaseOrderStatus("RECEIVED");
        purchaseOrderRepository.save(purchaseOrder);
        medicineService.refreshMedicineStockSummary(medicine.getMedicineId());
    }

    private MedicineBatch createBatch(PurchaseOrderReceiptForm form, Medicine medicine, Integer userId) {
        MedicineBatch batch = new MedicineBatch();
        batch.setMedicine(medicine);
        batch.setBatchNumber(form.getBatchNumber().trim());
        batch.setPurchasePrice(form.getPurchasePrice());
        batch.setSellingPrice(form.getSellingPrice());
        batch.setInitialQuantity(0);
        batch.setAvailableQuantity(0);
        batch.setReceivedDate(form.getReceivedDate());
        batch.setExpiryDate(form.getExpiryDate());
        batch.setActive(true);
        batch.setCreatedByUserId(userId);
        return medicineBatchRepository.save(batch);
    }

    private BranchStock createEmptyBranchStock(PharmacyBranch branch, MedicineBatch batch) {
        BranchStock stock = new BranchStock();
        stock.setBranch(branch);
        stock.setBatch(batch);
        stock.setQuantityInStock(0);
        stock.setUpdatedAt(LocalDateTime.now());
        return stock;
    }

    private void createStockTransaction(MedicineBatch batch,
                                        PharmacyBranch branch,
                                        PurchaseOrderReceiptForm form,
                                        Integer purchaseOrderId,
                                        Integer userId) {
        StockTransaction transaction = new StockTransaction();
        transaction.setBatch(batch);
        transaction.setBranch(branch);
        transaction.setTransactionType("STOCK_IN");
        transaction.setQuantityChange(form.getReceivedQuantity());
        transaction.setReferenceType("PURCHASE_ORDER");
        transaction.setReferenceId(purchaseOrderId);
        transaction.setNotes(trimToNull(form.getNotes()));
        transaction.setCreatedByUserId(userId);
        stockTransactionRepository.save(transaction);
    }

    private void createReceipt(PurchaseOrder purchaseOrder,
                               Medicine medicine,
                               MedicineBatch batch,
                               PharmacyBranch branch,
                               PurchaseOrderReceiptForm form,
                               Integer userId) {
        PurchaseOrderReceipt receipt = new PurchaseOrderReceipt();
        receipt.setPurchaseOrder(purchaseOrder);
        receipt.setMedicine(medicine);
        receipt.setBatch(batch);
        receipt.setBranch(branch);
        receipt.setBatchNumber(batch.getBatchNumber());
        receipt.setReceivedQuantity(form.getReceivedQuantity());
        receipt.setPurchasePrice(form.getPurchasePrice());
        receipt.setSellingPrice(form.getSellingPrice());
        receipt.setReceivedDate(form.getReceivedDate());
        receipt.setExpiryDate(form.getExpiryDate());
        receipt.setNotes(trimToNull(form.getNotes()));
        receipt.setReceivedByUserId(userId);
        receiptRepository.save(receipt);
    }

