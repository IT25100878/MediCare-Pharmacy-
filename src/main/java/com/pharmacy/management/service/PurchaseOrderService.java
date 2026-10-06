package com.pharmacy.management.service;


import com.pharmacy.management.entity.PurchaseOrder;
import com.pharmacy.management.entity.Supplier;
import com.pharmacy.management.repository.PurchaseOrderRepository;
import com.pharmacy.management.repository.SupplierRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final CurrentUserService currentUserService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                SupplierRepository supplierRepository,
                                CurrentUserService currentUserService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.currentUserService = currentUserService;
    }

    public List<PurchaseOrder> findAll(String keyword) {
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");
        if (keyword == null || keyword.isBlank()) {
            return purchaseOrderRepository.findAll(sort);
        }
        return purchaseOrderRepository.findByPurchaseOrderNumberContainingIgnoreCase(keyword.trim(), sort);
    }

    public List<Supplier> findAllSuppliers() {
        return supplierRepository.findAll(Sort.by(Sort.Direction.ASC, "supplierName"));
    }

    public PurchaseOrder findById(Integer purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found."));
    }

    @Transactional
    public void create(PurchaseOrder purchaseOrder, String currentUserEmail) {
        requireSupplier(purchaseOrder.getSupplierId());
        purchaseOrder.setPurchaseOrderId(null);
        purchaseOrder.setProcurementUserId(currentUserService.getUserId(currentUserEmail));
        purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public void update(Integer purchaseOrderId, PurchaseOrder formPurchaseOrder) {
        requireSupplier(formPurchaseOrder.getSupplierId());
        PurchaseOrder existing = findById(purchaseOrderId);
        existing.setPurchaseOrderNumber(formPurchaseOrder.getPurchaseOrderNumber());
        existing.setSupplierId(formPurchaseOrder.getSupplierId());
        existing.setOrderDate(formPurchaseOrder.getOrderDate());
        existing.setExpectedDate(formPurchaseOrder.getExpectedDate());
        existing.setTotalAmount(formPurchaseOrder.getTotalAmount());
        existing.setPurchaseOrderStatus(formPurchaseOrder.getPurchaseOrderStatus());
        purchaseOrderRepository.save(existing);
    }
