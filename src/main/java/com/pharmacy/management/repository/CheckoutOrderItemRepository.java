package com.pharmacy.management.repository;

import com.pharmacy.management.entity.CheckoutOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckoutOrderItemRepository extends JpaRepository<CheckoutOrderItem, Integer> {

    List<CheckoutOrderItem> findByOrderId(Integer orderId);
}
