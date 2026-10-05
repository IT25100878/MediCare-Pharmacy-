package com.pharmacy.management.dto;

import com.pharmacy.management.entity.Promotion;

import java.math.BigDecimal;

public record PromotionDiscount(Promotion promotion, BigDecimal discountAmount) {

    public static PromotionDiscount none() {
        return new PromotionDiscount(null, BigDecimal.ZERO);
    }
}
