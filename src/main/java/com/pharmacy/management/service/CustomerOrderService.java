package com.pharmacy.management.service;

import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.repository.CustomerOrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CurrentUserService currentUserService;

    public CustomerOrderService(CustomerOrderRepository customerOrderRepository,
                                CurrentUserService currentUserService) {
        this.customerOrderRepository = customerOrderRepository;
        this.currentUserService = currentUserService;
    }

    public List<CustomerOrder> findAll(String keyword) {
        Sort sort = Sort.by(Sort.Direction.DESC, "orderDate");
        if (keyword == null || keyword.isBlank()) {
            return customerOrderRepository.findAll(sort);
        }
        String text = keyword.trim();
        return customerOrderRepository
                .findByOrderNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrCustomerPhoneContainingIgnoreCase(
                        text, text, text, sort
                );
    }

    public CustomerOrder findById(Integer orderId) {
        return customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));
    }

    @Transactional
    public void create(CustomerOrder order, String currentUserEmail) {
        order.setOrderId(null);
        order.setCashierUserId(currentUserService.getUserId(currentUserEmail));
        customerOrderRepository.save(order);
    }

    @Transactional
    public void update(Integer orderId, CustomerOrder formOrder) {
        CustomerOrder existing = findById(orderId);
        existing.setOrderNumber(formOrder.getOrderNumber());
        existing.setCustomerName(formOrder.getCustomerName());
        existing.setCustomerPhone(formOrder.getCustomerPhone());
        existing.setOrderDate(formOrder.getOrderDate());
        existing.setTotalAmount(formOrder.getTotalAmount());
        existing.setDiscountAmount(formOrder.getDiscountAmount());
        existing.setPaymentStatus(formOrder.getPaymentStatus());
        existing.setOrderStatus(formOrder.getOrderStatus());
        customerOrderRepository.save(existing);
    }

    @Transactional
    public void delete(Integer orderId) {
        CustomerOrder order = findById(orderId);
        if ("PAID".equals(order.getPaymentStatus()) || "COMPLETED".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("A paid or completed order must remain in the sales audit. Use the Invoice Centre for document actions.");
        }
        order.setOrderStatus("CANCELLED");
        customerOrderRepository.save(order);
    }
}

