package com.pharmacy.management.dto;

public class InventoryDashboardSummary {

    private final long medicineCount;
    private final long batchCount;
    private final long lowStockCount;
    private final long expiryAlertCount;
    private final long inTransitTransferCount;

    public InventoryDashboardSummary(long medicineCount,
                                     long batchCount,
                                     long lowStockCount,
                                     long expiryAlertCount,
                                     long inTransitTransferCount) {
        this.medicineCount = medicineCount;
        this.batchCount = batchCount;
        this.lowStockCount = lowStockCount;
        this.expiryAlertCount = expiryAlertCount;
        this.inTransitTransferCount = inTransitTransferCount;
    }

    public long getMedicineCount() { return medicineCount; }
    public long getBatchCount() { return batchCount; }
    public long getLowStockCount() { return lowStockCount; }
    public long getExpiryAlertCount() { return expiryAlertCount; }
    public long getInTransitTransferCount() { return inTransitTransferCount; }
}
