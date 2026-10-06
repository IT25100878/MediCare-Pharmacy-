package com.pharmacy.management.service;

import com.pharmacy.management.entity.BranchStock;
import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.entity.MedicineBatch;
import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.entity.StockTransaction;
import com.pharmacy.management.repository.AppUserRepository;
import com.pharmacy.management.repository.BranchStockRepository;
import com.pharmacy.management.repository.MedicineBatchRepository;
import com.pharmacy.management.repository.MedicineRepository;
import com.pharmacy.management.repository.PermanentDeletionRepository;
import com.pharmacy.management.repository.PharmacyBranchRepository;
import com.pharmacy.management.repository.StockTransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final AppUserRepository appUserRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final BranchStockRepository branchStockRepository;
    private final PharmacyBranchRepository pharmacyBranchRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final PermanentDeletionRepository permanentDeletionRepository;

    public MedicineService(MedicineRepository medicineRepository,
                           AppUserRepository appUserRepository,
                           MedicineBatchRepository medicineBatchRepository,
                           BranchStockRepository branchStockRepository,
                           PharmacyBranchRepository pharmacyBranchRepository,
                           StockTransactionRepository stockTransactionRepository,
                           PermanentDeletionRepository permanentDeletionRepository) {
        this.medicineRepository = medicineRepository;
        this.appUserRepository = appUserRepository;
        this.medicineBatchRepository = medicineBatchRepository;
        this.branchStockRepository = branchStockRepository;
        this.pharmacyBranchRepository = pharmacyBranchRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.permanentDeletionRepository = permanentDeletionRepository;
    }

    public List<Medicine> findAll(String keyword) {
        Sort sort = Sort.by(Sort.Direction.ASC, "medicineName", "batchNumber");
        if (keyword == null || keyword.isBlank()) {
            return medicineRepository.findAll(sort);
        }

        String searchText = keyword.trim();
        return medicineRepository
                .findByMedicineNameContainingIgnoreCaseOrGenericNameContainingIgnoreCaseOrBatchNumberContainingIgnoreCase(
                        searchText, searchText, searchText, sort
                );
    }

    public Medicine findById(Integer medicineId) {
        return medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found."));
    }

    @Transactional
    public void create(Medicine medicine, String currentUserEmail) {
        Integer userId = appUserRepository.findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user was not found."))
                .getUserId();

        medicine.setMedicineId(null);
        medicine.setCreatedByUserId(userId);
        medicine.setUpdatedAt(null);
        Medicine savedMedicine = medicineRepository.save(medicine);

        MedicineBatch firstBatch = new MedicineBatch();
        firstBatch.setMedicine(savedMedicine);
        firstBatch.setBatchNumber(savedMedicine.getBatchNumber().trim());
        firstBatch.setPurchasePrice(nonNullMoney(savedMedicine.getPurchasePrice()));
        firstBatch.setSellingPrice(nonNullMoney(savedMedicine.getSellingPrice()));
        firstBatch.setInitialQuantity(nonNullQuantity(savedMedicine.getQuantityInStock()));
        firstBatch.setAvailableQuantity(nonNullQuantity(savedMedicine.getQuantityInStock()));
        firstBatch.setReceivedDate(LocalDate.now());
        firstBatch.setExpiryDate(savedMedicine.getExpiryDate());
        firstBatch.setActive(true);
        firstBatch.setCreatedByUserId(userId);
        MedicineBatch savedBatch = medicineBatchRepository.save(firstBatch);

        PharmacyBranch centralWarehouse = ensureCentralWarehouse();
        BranchStock openingStock = new BranchStock();
        openingStock.setBatch(savedBatch);
        openingStock.setBranch(centralWarehouse);
        openingStock.setQuantityInStock(savedBatch.getAvailableQuantity());
        openingStock.setUpdatedAt(LocalDateTime.now());
        branchStockRepository.save(openingStock);

        if (savedBatch.getAvailableQuantity() > 0) {
            createTransaction(savedBatch, centralWarehouse, "OPENING_BALANCE", savedBatch.getAvailableQuantity(),
                    "MEDICINE_CREATE", savedMedicine.getMedicineId(), "Opening quantity when medicine was created.", userId);
        }
    }

    @Transactional
    public void update(Integer medicineId, Medicine formMedicine) {
        Medicine existingMedicine = findById(medicineId);
        existingMedicine.setMedicineName(formMedicine.getMedicineName());
        existingMedicine.setGenericName(formMedicine.getGenericName());
        existingMedicine.setCategory(formMedicine.getCategory());
        existingMedicine.setPurchasePrice(formMedicine.getPurchasePrice());
        existingMedicine.setSellingPrice(formMedicine.getSellingPrice());
        existingMedicine.setReorderLevel(formMedicine.getReorderLevel());
        existingMedicine.setRequiresPrescription(formMedicine.isRequiresPrescription());
        existingMedicine.setUpdatedAt(LocalDateTime.now());

        // Batch, expiry, and quantity are intentionally changed only by stock workflows.
        medicineRepository.save(existingMedicine);
    }

    @Transactional
    public void delete(Integer medicineId) {
        findById(medicineId);
        permanentDeletionRepository.deleteMedicine(medicineId);
    }

    public long countLowStock() {
        return medicineRepository.countLowStockMedicines();
    }

    public long countExpiringSoon() {
        return medicineRepository.countMedicinesExpiringBy(LocalDate.now().plusDays(30));
    }

    @Transactional
    public void refreshMedicineStockSummary(Integer medicineId) {
        Medicine medicine = findById(medicineId);
        List<BranchStock> branchStocks = branchStockRepository.findByMedicineId(medicineId);
        int totalQuantity = branchStocks.stream()
                .map(BranchStock::getQuantityInStock)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum();

        medicine.setQuantityInStock(totalQuantity);
        medicineBatchRepository.findByMedicine_MedicineIdOrderByExpiryDateAsc(medicineId).stream()
                .filter(batch -> batch.getAvailableQuantity() != null && batch.getAvailableQuantity() > 0)
                .min(Comparator.comparing(MedicineBatch::getExpiryDate))
                .map(MedicineBatch::getExpiryDate)
                .ifPresent(medicine::setExpiryDate);
        medicine.setUpdatedAt(LocalDateTime.now());
        medicineRepository.save(medicine);
    }

    private PharmacyBranch ensureCentralWarehouse() {
        return pharmacyBranchRepository.findByBranchNameIgnoreCase("Central Warehouse")
                .orElseGet(() -> {
                    PharmacyBranch branch = new PharmacyBranch();
                    branch.setBranchName("Central Warehouse");
                    branch.setAddress("MediCare Central Stock Location");
                    branch.setContactNumber("0000000000");
                    branch.setManagerName("System");
                    branch.setActive(true);
                    return pharmacyBranchRepository.save(branch);
                });
    }

    private void createTransaction(MedicineBatch batch,
                                   PharmacyBranch branch,
                                   String type,
                                   int quantityChange,
                                   String referenceType,
                                   Integer referenceId,
                                   String notes,
                                   Integer userId) {
        StockTransaction transaction = new StockTransaction();
        transaction.setBatch(batch);
        transaction.setBranch(branch);
        transaction.setTransactionType(type);
        transaction.setQuantityChange(quantityChange);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);
        transaction.setCreatedByUserId(userId);
        stockTransactionRepository.save(transaction);
    }

    private BigDecimal nonNullMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int nonNullQuantity(Integer value) {
        return value == null ? 0 : value;
    }
}
