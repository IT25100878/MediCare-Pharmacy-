package com.pharmacy.management.repository;

import com.pharmacy.management.entity.PurchaseOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    List<PurchaseOrder> findByPurchaseOrderNumberContainingIgnoreCase(String purchaseOrderNumber, Sort sort);
}


