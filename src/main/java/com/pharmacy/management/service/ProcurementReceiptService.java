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
