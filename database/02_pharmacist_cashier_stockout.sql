/*
   MEDICARE PHARMACY - PHARMACIST TO CASHIER TO STOCK-OUT CONNECTION
   Run after the All-Role CRUD and Inventory Connected Workflow migrations.
*/
USE PharmacyManagementDB;
GO

/* Inventory Supervisor marks which medicines need a verified prescription. */
IF COL_LENGTH(N'dbo.Medicines', N'RequiresPrescription') IS NULL
BEGIN
    ALTER TABLE dbo.Medicines
        ADD RequiresPrescription BIT NOT NULL
            CONSTRAINT DF_Medicines_RequiresPrescription DEFAULT (0) WITH VALUES;
END;
GO

/* Connect a completed cashier checkout to its branch, payment method, and (if required) prescription. */
IF COL_LENGTH(N'dbo.CustomerOrders', N'BranchId') IS NULL
BEGIN
    ALTER TABLE dbo.CustomerOrders ADD BranchId INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.CustomerOrders', N'PrescriptionId') IS NULL
BEGIN
    ALTER TABLE dbo.CustomerOrders ADD PrescriptionId INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.CustomerOrders', N'PaymentMethod') IS NULL
BEGIN
    ALTER TABLE dbo.CustomerOrders
        ADD PaymentMethod NVARCHAR(20) NOT NULL
            CONSTRAINT DF_CustomerOrders_PaymentMethod DEFAULT (N'CASH') WITH VALUES;
END;
GO

IF COL_LENGTH(N'dbo.CustomerOrders', N'FulfilledAt') IS NULL
BEGIN
    ALTER TABLE dbo.CustomerOrders ADD FulfilledAt DATETIME2(0) NULL;
END;
GO

/* Each checkout item remembers the exact batch that was released. */
IF COL_LENGTH(N'dbo.OrderItems', N'BatchId') IS NULL
BEGIN
    ALTER TABLE dbo.OrderItems ADD BatchId INT NULL;
END;
GO

/* A verified prescription can be dispensed only once in this demo workflow. */
IF COL_LENGTH(N'dbo.Prescriptions', N'DispensedOrderId') IS NULL
BEGIN
    ALTER TABLE dbo.Prescriptions ADD DispensedOrderId INT NULL;
END;
GO

IF COL_LENGTH(N'dbo.Prescriptions', N'DispensedAt') IS NULL
BEGIN
    ALTER TABLE dbo.Prescriptions ADD DispensedAt DATETIME2(0) NULL;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_CustomerOrders_Branch')
BEGIN
    ALTER TABLE dbo.CustomerOrders
        ADD CONSTRAINT FK_CustomerOrders_Branch FOREIGN KEY (BranchId)
            REFERENCES dbo.PharmacyBranches(BranchId) ON DELETE NO ACTION;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_CustomerOrders_Prescription')
BEGIN
    ALTER TABLE dbo.CustomerOrders
        ADD CONSTRAINT FK_CustomerOrders_Prescription FOREIGN KEY (PrescriptionId)
            REFERENCES dbo.Prescriptions(PrescriptionId) ON DELETE NO ACTION;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_OrderItems_Batch')
BEGIN
    ALTER TABLE dbo.OrderItems
        ADD CONSTRAINT FK_OrderItems_Batch FOREIGN KEY (BatchId)
            REFERENCES dbo.MedicineBatches(BatchId) ON DELETE NO ACTION;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_Prescriptions_DispensedOrder')
BEGIN
    ALTER TABLE dbo.Prescriptions
        ADD CONSTRAINT FK_Prescriptions_DispensedOrder FOREIGN KEY (DispensedOrderId)
            REFERENCES dbo.CustomerOrders(OrderId) ON DELETE NO ACTION;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_CustomerOrders_PaymentMethod')
BEGIN
    ALTER TABLE dbo.CustomerOrders
        ADD CONSTRAINT CK_CustomerOrders_PaymentMethod
            CHECK (PaymentMethod IN (N'CASH', N'CARD', N'ONLINE'));
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_CustomerOrders_BranchFulfilled'
      AND object_id = OBJECT_ID(N'dbo.CustomerOrders')
)
BEGIN
    CREATE INDEX IX_CustomerOrders_BranchFulfilled
        ON dbo.CustomerOrders(BranchId, FulfilledAt DESC);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_Prescriptions_CheckoutQueue'
      AND object_id = OBJECT_ID(N'dbo.Prescriptions')
)
BEGIN
    CREATE INDEX IX_Prescriptions_CheckoutQueue
        ON dbo.Prescriptions(PrescriptionStatus, DispensedAt);
END;
GO
