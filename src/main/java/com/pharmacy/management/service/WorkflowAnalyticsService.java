package com.pharmacy.management.service;

import com.pharmacy.management.dto.InventoryDashboardSummary;
import com.pharmacy.management.dto.WorkflowAnalyticsSnapshot;
import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.entity.Delivery;
import com.pharmacy.management.entity.Feedback;
import com.pharmacy.management.repository.CustomerOrderRepository;
import com.pharmacy.management.repository.DeliveryRepository;
import com.pharmacy.management.repository.FeedbackRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WorkflowAnalyticsService {

    private final CustomerOrderRepository customerOrderRepository;
    private final DeliveryRepository deliveryRepository;
    private final FeedbackRepository feedbackRepository;
    private final InventoryWorkflowService inventoryWorkflowService;

    public WorkflowAnalyticsService(CustomerOrderRepository customerOrderRepository,
                                    DeliveryRepository deliveryRepository,
                                    FeedbackRepository feedbackRepository,
                                    InventoryWorkflowService inventoryWorkflowService) {
        this.customerOrderRepository = customerOrderRepository;
        this.deliveryRepository = deliveryRepository;
        this.feedbackRepository = feedbackRepository;
        this.inventoryWorkflowService = inventoryWorkflowService;
    }

    public WorkflowAnalyticsSnapshot getSnapshot() {
        List<CustomerOrder> orders = customerOrderRepository.findAll();
        List<Delivery> deliveries = deliveryRepository.findAll();
        long paidOrders = orders.stream().filter(order -> "PAID".equals(order.getPaymentStatus())).count();
        BigDecimal revenue = orders.stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()))
                .map(order -> {
                    BigDecimal total = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
                    BigDecimal discount = order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount();
                    return total.subtract(discount).max(BigDecimal.ZERO);
                })
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingDeliveries = deliveries.stream().filter(delivery -> "PENDING".equals(delivery.getDeliveryStatus())).count();
        long activeDeliveries = deliveries.stream()
                .filter(delivery -> "ASSIGNED".equals(delivery.getDeliveryStatus()) || "OUT_FOR_DELIVERY".equals(delivery.getDeliveryStatus()))
                .count();
        long delivered = deliveries.stream().filter(delivery -> "DELIVERED".equals(delivery.getDeliveryStatus())).count();
        InventoryDashboardSummary inventory = inventoryWorkflowService.getDashboardSummary();
        List<Feedback> feedbackItems = feedbackRepository.findByArchivedFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
        long newFeedback = feedbackItems.stream().filter(feedback -> "NEW".equals(feedback.getFeedbackStatus())).count();
        double averageRating = feedbackItems.stream()
                .map(Feedback::getRating)
                .filter(rating -> rating != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return new WorkflowAnalyticsSnapshot(
                paidOrders,
                revenue,
                pendingDeliveries,
                activeDeliveries,
                delivered,
                inventory.getLowStockCount(),
                inventory.getExpiryAlertCount(),
                inventory.getInTransitTransferCount(),
                feedbackItems.size(),
                newFeedback,
                averageRating
        );
    }

    public List<CustomerOrder> findRecentPaidOrders() {
        return customerOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate")).stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()))
                .limit(8)
                .toList();
    }

    public List<Delivery> findRecentDeliveries() {
        return deliveryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .limit(8)
                .toList();
    }
}

