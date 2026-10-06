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
