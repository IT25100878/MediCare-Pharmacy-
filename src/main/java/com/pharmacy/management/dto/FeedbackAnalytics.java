package com.pharmacy.management.dto;

public record FeedbackAnalytics(long totalCases,
                                long newCases,
                                long inProgressCases,
                                long resolvedCases,
                                long closedCases,
                                long urgentCases,
                                double averageRating) {
}