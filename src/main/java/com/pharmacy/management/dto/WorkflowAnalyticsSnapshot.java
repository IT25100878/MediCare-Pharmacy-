package com.pharmacy.management.dto;

import java.math.BigDecimal;

public class WorkflowAnalyticsSnapshot {

    private final long paidOrderCount;
    private final BigDecimal paidRevenue;
    private final long pendingDeliveryCount;
    private final long activeDeliveryCount;
    private final long deliveredCount;
    private final long lowStockCount;
    private final long expiryAlertCount;
    private final long inTransitTransferCount;
    private final long feedbackCount;
    private final long newFeedbackCount;
    private final double averageFeedbackRating;

    public WorkflowAnalyticsSnapshot(long paidOrderCount,
                                     BigDecimal paidRevenue,
                                     long pendingDeliveryCount,
                                     long activeDeliveryCount,
                                     long deliveredCount,
                                     long lowStockCount,
                                     long expiryAlertCount,
                                     long inTransitTransferCount,
                                     long feedbackCount,
                                     long newFeedbackCount,
                                     double averageFeedbackRating) {
        this.paidOrderCount = paidOrderCount;
        this.paidRevenue = paidRevenue;
        this.pendingDeliveryCount = pendingDeliveryCount;
        this.activeDeliveryCount = activeDeliveryCount;
        this.deliveredCount = deliveredCount;
        this.lowStockCount = lowStockCount;
        this.expiryAlertCount = expiryAlertCount;
        this.inTransitTransferCount = inTransitTransferCount;
        this.feedbackCount = feedbackCount;
        this.newFeedbackCount = newFeedbackCount;
        this.averageFeedbackRating = averageFeedbackRating;
    }

    public long getPaidOrderCount() { return paidOrderCount; }
    public BigDecimal getPaidRevenue() { return paidRevenue; }
    public long getPendingDeliveryCount() { return pendingDeliveryCount; }
    public long getActiveDeliveryCount() { return activeDeliveryCount; }
    public long getDeliveredCount() { return deliveredCount; }
    public long getLowStockCount() { return lowStockCount; }
    public long getExpiryAlertCount() { return expiryAlertCount; }
    public long getInTransitTransferCount() { return inTransitTransferCount; }
    public long getFeedbackCount() { return feedbackCount; }
    public long getNewFeedbackCount() { return newFeedbackCount; }
    public double getAverageFeedbackRating() { return averageFeedbackRating; }

    public long getTrackedDeliveryCount() {
        return pendingDeliveryCount + activeDeliveryCount + deliveredCount;
    }

    public int getDeliveryCompletionPercent() {
        long tracked = getTrackedDeliveryCount();
        return tracked == 0 ? 0 : (int) Math.round((deliveredCount * 100.0) / tracked);
    }
}

