package com.pharmacy.management.repository;

import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.entity.CustomerOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {

    List<CustomerOrder> findByOrderNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrCustomerPhoneContainingIgnoreCase(
            String orderNumber,
            String customerName,
            String customerPhone,
            Sort sort
    );
}
