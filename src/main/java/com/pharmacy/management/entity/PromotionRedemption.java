package com.pharmacy.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PromotionRedemptions", schema = "dbo")
public class PromotionRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionRedemptionId")
    private Integer promotionRedemptionId;

    @Column(name = "PromotionId", nullable = false)
    private Integer promotionId;

    @Column(name = "OrderId", nullable = false)
    private Integer orderId;

    @Column(name = "DiscountAmount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "RedeemedAt", insertable = false, updatable = false)
    private LocalDateTime redeemedAt;

    public Integer getPromotionRedemptionId() { return promotionRedemptionId; }
    public Integer getPromotionId() { return promotionId; }
    public void setPromotionId(Integer promotionId) { this.promotionId = promotionId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public LocalDateTime getRedeemedAt() { return redeemedAt; }
}

