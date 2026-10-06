package com.pharmacy.management.service;

import com.pharmacy.management.dto.InventoryAlert;
import com.pharmacy.management.dto.InventoryDashboardSummary;
import com.pharmacy.management.dto.StockReceiptForm;
import com.pharmacy.management.dto.StockTransferForm;
import com.pharmacy.management.entity.BranchStock;
import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.entity.MedicineBatch;
import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.entity.StockTransaction;
import com.pharmacy.management.entity.StockTransfer;
import com.pharmacy.management.repository.BranchStockRepository;
import com.pharmacy.management.repository.MedicineBatchRepository;
import com.pharmacy.management.repository.MedicineRepository;
import com.pharmacy.management.repository.PharmacyBranchRepository;
import com.pharmacy.management.repository.StockTransactionRepository;
import com.pharmacy.management.repository.StockTransferRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryWorkflowService {

    private static final int EXPIRY_ALERT_DAYS = 30;

    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final BranchStockRepository branchStockRepository;
    private final PharmacyBranchRepository pharmacyBranchRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockTransferRepository stockTransferRepository;
    private final CurrentUserService currentUserService;
    private final MedicineService medicineService;

    public InventoryWorkflowService(MedicineRepository medicineRepository,
                                    MedicineBatchRepository medicineBatchRepository,
                                    BranchStockRepository branchStockRepository,
                                    PharmacyBranchRepository pharmacyBranchRepository,
                                    StockTransactionRepository stockTransactionRepository,
                                    StockTransferRepository stockTransferRepository,
                                    CurrentUserService currentUserService,
                                    MedicineService medicineService) {
        this.medicineRepository = medicineRepository;
        this.medicineBatchRepository = medicineBatchRepository;
        this.branchStockRepository = branchStockRepository;
        this.pharmacyBranchRepository = pharmacyBranchRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.stockTransferRepository = stockTransferRepository;
        this.currentUserService = currentUserService;
        this.medicineService = medicineService;
    }

    public InventoryDashboardSummary getDashboardSummary() {
        long medicineCount = medicineRepository.count();
        long batchCount = medicineBatchRepository.countByActiveTrue();
        long lowStockCount = medicineRepository.findAll().stream()
                .filter(this::isLowStock)
                .count();
        long expiryCount = medicineBatchRepository.findExpiringBy(LocalDate.now().plusDays(EXPIRY_ALERT_DAYS)).size();
        long inTransitCount = stockTransferRepository.countByTransferStatus("IN_TRANSIT");
        return new InventoryDashboardSummary(medicineCount, batchCount, lowStockCount, expiryCount, inTransitCount);
    }

    public List<InventoryAlert> getAlerts() {
        List<InventoryAlert> alerts = new ArrayList<>();

        medicineRepository.findAll(Sort.by("medicineName")).stream()
                .filter(this::isLowStock)
                .forEach(medicine -> alerts.add(new InventoryAlert(
                        "LOW_STOCK",
                        "Low stock: " + medicine.getMedicineName(),
                        "Available " + medicine.getQuantityInStock() + " unit(s); reorder level is " + medicine.getReorderLevel() + "."
                )));

        medicineBatchRepository.findExpiringBy(LocalDate.now().plusDays(EXPIRY_ALERT_DAYS)).forEach(batch -> alerts.add(new InventoryAlert(
                "EXPIRY",
                "Expiry watch: " + batch.getMedicine().getMedicineName() + " · " + batch.getBatchNumber(),
                batch.getAvailableQuantity() + " unit(s) expire on " + batch.getExpiryDate() + "."
        )));

        return alerts.stream()
                .sorted(Comparator.comparing(InventoryAlert::getLevel).thenComparing(InventoryAlert::getTitle))
                .toList();
    }

    public List<MedicineBatch> getActiveBatches() {
        return medicineBatchRepository.findByActiveTrueOrderByExpiryDateAsc();
    }

    public List<PharmacyBranch> getActiveBranches() {
        return pharmacyBranchRepository.findByActiveTrueOrderByBranchNameAsc();
    }

    public List<BranchStock> getBranchStocks(Integer medicineId) {
        if (medicineId == null) {
            return branchStockRepository.findAllForInventory();
        }
        return branchStockRepository.findByMedicineId(medicineId);
    }

    public List<StockTransaction> getRecentTransactions() {
        List<StockTransaction> allTransactions = stockTransactionRepository.findRecentTransactions();
        return allTransactions.subList(0, Math.min(allTransactions.size(), 15));
    }

    public List<StockTransfer> getTransfers() {
        return stockTransferRepository.findAllByOrderByRequestedAtDesc();
    }

    @Transactional
    public void receiveStock(StockReceiptForm form, String currentUserEmail) {
        validateSellingPrice(form.getPurchasePrice(), form.getSellingPrice());
        Medicine medicine = requireMedicine(form.getMedicineId());
        PharmacyBranch branch = requireActiveBranch(form.getBranchId());
        Integer userId = currentUserService.getUserId(currentUserEmail);

        MedicineBatch batch = medicineBatchRepository
                .findByMedicine_MedicineIdAndBatchNumberIgnoreCase(medicine.getMedicineId(), form.getBatchNumber().trim())
                .orElseGet(() -> createNewBatch(form, medicine, userId));

        if (batch.getBatchId() != null) {
            ensureCompatibleBatch(batch, form);
            batch.setInitialQuantity(batch.getInitialQuantity() + form.getReceivedQuantity());
            batch.setAvailableQuantity(batch.getAvailableQuantity() + form.getReceivedQuantity());
            batch.setPurchasePrice(form.getPurchasePrice());
            batch.setSellingPrice(form.getSellingPrice());
            batch.setReceivedDate(form.getReceivedDate());
            batch.setExpiryDate(form.getExpiryDate());
            batch = medicineBatchRepository.save(batch);
        }

        final MedicineBatch stockBatch = batch;

        BranchStock branchStock = branchStockRepository
                .findByBranch_BranchIdAndBatch_BatchId(branch.getBranchId(), stockBatch.getBatchId())
                .orElseGet(() -> createEmptyBranchStock(branch, stockBatch));
        branchStock.setQuantityInStock(branchStock.getQuantityInStock() + form.getReceivedQuantity());
        branchStock.setUpdatedAt(LocalDateTime.now());
        branchStockRepository.save(branchStock);

        medicine.setPurchasePrice(form.getPurchasePrice());
        medicine.setSellingPrice(form.getSellingPrice());
        medicine.setUpdatedAt(LocalDateTime.now());
        medicineRepository.save(medicine);

        createTransaction(batch, branch, "STOCK_IN", form.getReceivedQuantity(), "STOCK_RECEIPT", batch.getBatchId(), form.getNotes(), userId);
        medicineService.refreshMedicineStockSummary(medicine.getMedicineId());
    }

    @Transactional
    public void requestTransfer(StockTransferForm form, String currentUserEmail) {
        validateDifferentBranches(form.getFromBranchId(), form.getToBranchId());
        MedicineBatch batch = requireBatch(form.getBatchId());
        PharmacyBranch fromBranch = requireActiveBranch(form.getFromBranchId());
        PharmacyBranch toBranch = requireActiveBranch(form.getToBranchId());
        ensureSourceHasQuantity(batch, fromBranch, form.getQuantity());

        StockTransfer transfer = new StockTransfer();
        transfer.setTransferNumber(createTransferNumber());
        transfer.setBatch(batch);
        transfer.setFromBranch(fromBranch);
        transfer.setToBranch(toBranch);
        transfer.setQuantity(form.getQuantity());
        transfer.setTransferStatus("PENDING");
        transfer.setNotes(trimToNull(form.getNotes()));
        transfer.setRequestedByUserId(currentUserService.getUserId(currentUserEmail));
        stockTransferRepository.save(transfer);
    }

    @Transactional
    public void dispatchTransfer(Integer transferId, String currentUserEmail) {
        StockTransfer transfer = requireTransfer(transferId);
        requireStatus(transfer, "PENDING", "Only a pending transfer can be dispatched.");
        BranchStock sourceStock = requireBranchStock(transfer.getFromBranch(), transfer.getBatch());
        if (sourceStock.getQuantityInStock() < transfer.getQuantity()) {
            throw new IllegalArgumentException("Source branch no longer has enough stock to dispatch this transfer.");
        }

        sourceStock.setQuantityInStock(sourceStock.getQuantityInStock() - transfer.getQuantity());
        sourceStock.setUpdatedAt(LocalDateTime.now());
        branchStockRepository.save(sourceStock);

        MedicineBatch batch = transfer.getBatch();
        batch.setAvailableQuantity(batch.getAvailableQuantity() - transfer.getQuantity());
        medicineBatchRepository.save(batch);

        Integer userId = currentUserService.getUserId(currentUserEmail);
        createTransaction(batch, transfer.getFromBranch(), "TRANSFER_OUT", -transfer.getQuantity(),
                "STOCK_TRANSFER", transfer.getStockTransferId(), transfer.getNotes(), userId);
        transfer.setTransferStatus("IN_TRANSIT");
        transfer.setDispatchedByUserId(userId);
        transfer.setDispatchedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        medicineService.refreshMedicineStockSummary(batch.getMedicine().getMedicineId());
    }

    @Transactional
    public void receiveTransfer(Integer transferId, String currentUserEmail) {
        StockTransfer transfer = requireTransfer(transferId);
        requireStatus(transfer, "IN_TRANSIT", "Only an in-transit transfer can be received.");
        MedicineBatch batch = transfer.getBatch();
        PharmacyBranch destination = transfer.getToBranch();
        BranchStock destinationStock = branchStockRepository.findByBranch_BranchIdAndBatch_BatchId(destination.getBranchId(), batch.getBatchId())
                .orElseGet(() -> createEmptyBranchStock(destination, batch));

        destinationStock.setQuantityInStock(destinationStock.getQuantityInStock() + transfer.getQuantity());
        destinationStock.setUpdatedAt(LocalDateTime.now());
        branchStockRepository.save(destinationStock);

        batch.setAvailableQuantity(batch.getAvailableQuantity() + transfer.getQuantity());
        medicineBatchRepository.save(batch);

        Integer userId = currentUserService.getUserId(currentUserEmail);
        createTransaction(batch, destination, "TRANSFER_IN", transfer.getQuantity(),
                "STOCK_TRANSFER", transfer.getStockTransferId(), transfer.getNotes(), userId);
        transfer.setTransferStatus("COMPLETED");
        transfer.setReceivedByUserId(userId);
        transfer.setReceivedAt(LocalDateTime.now());
        stockTransferRepository.save(transfer);
        medicineService.refreshMedicineStockSummary(batch.getMedicine().getMedicineId());
    }

    @Transactional
    public void cancelTransfer(Integer transferId) {
        StockTransfer transfer = requireTransfer(transferId);
        requireStatus(transfer, "PENDING", "Only a pending transfer can be cancelled.");
        transfer.setTransferStatus("CANCELLED");
        stockTransferRepository.save(transfer);
    }

    private MedicineBatch createNewBatch(StockReceiptForm form, Medicine medicine, Integer userId) {
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
        BranchStock branchStock = new BranchStock();
        branchStock.setBranch(branch);
        branchStock.setBatch(batch);
        branchStock.setQuantityInStock(0);
        branchStock.setUpdatedAt(LocalDateTime.now());
        return branchStock;
    }

    private void createTransaction(MedicineBatch batch,
                                   PharmacyBranch branch,
                                   String transactionType,
                                   int quantityChange,
                                   String referenceType,
                                   Integer referenceId,
                                   String notes,
                                   Integer userId) {
        StockTransaction transaction = new StockTransaction();
        transaction.setBatch(batch);
        transaction.setBranch(branch);
        transaction.setTransactionType(transactionType);
        transaction.setQuantityChange(quantityChange);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(trimToNull(notes));
        transaction.setCreatedByUserId(userId);
        stockTransactionRepository.save(transaction);
    }

    private void ensureCompatibleBatch(MedicineBatch batch, StockReceiptForm form) {
        if (batch.getExpiryDate() != null && !batch.getExpiryDate().equals(form.getExpiryDate())) {
            throw new IllegalArgumentException("This batch already exists with a different expiry date. Use the correct expiry date or create a new batch number.");
        }
    }

    private void validateSellingPrice(java.math.BigDecimal purchasePrice, java.math.BigDecimal sellingPrice) {
        if (purchasePrice != null && sellingPrice != null && sellingPrice.compareTo(purchasePrice) < 0) {
            throw new IllegalArgumentException("Selling price must be equal to or greater than purchase price.");
        }
    }

    private void validateDifferentBranches(Integer fromBranchId, Integer toBranchId) {
        if (fromBranchId != null && fromBranchId.equals(toBranchId)) {
            throw new IllegalArgumentException("Source and destination branches must be different.");
        }
    }

    private void ensureSourceHasQuantity(MedicineBatch batch, PharmacyBranch branch, Integer quantity) {
        BranchStock sourceStock = requireBranchStock(branch, batch);
        if (quantity == null || sourceStock.getQuantityInStock() < quantity) {
            throw new IllegalArgumentException("The source branch does not have enough stock for this transfer.");
        }
    }

    private BranchStock requireBranchStock(PharmacyBranch branch, MedicineBatch batch) {
        return branchStockRepository.findByBranch_BranchIdAndBatch_BatchId(branch.getBranchId(), batch.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException("This batch is not available at the selected source branch."));
    }

    private Medicine requireMedicine(Integer medicineId) {
        return medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found."));
    }

    private MedicineBatch requireBatch(Integer batchId) {
        return medicineBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine batch not found."));
    }

    private PharmacyBranch requireActiveBranch(Integer branchId) {
        PharmacyBranch branch = pharmacyBranchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found."));
        if (!branch.isActive()) {
            throw new IllegalArgumentException("Choose an active branch.");
        }
        return branch;
    }

    private StockTransfer requireTransfer(Integer transferId) {
        return stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock transfer not found."));
    }

    private void requireStatus(StockTransfer transfer, String expectedStatus, String errorMessage) {
        if (!expectedStatus.equals(transfer.getTransferStatus())) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private boolean isLowStock(Medicine medicine) {
        return medicine.getQuantityInStock() != null
                && medicine.getReorderLevel() != null
                && medicine.getQuantityInStock() <= medicine.getReorderLevel();
    }

    private String createTransferNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TRF-" + date + "-" + random;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

