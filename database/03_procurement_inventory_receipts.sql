/*
   MEDICARE PHARMACY - PROCUREMENT TO INVENTORY CONNECTION
   Run this only AFTER Inventory_Connected_Workflow_Migration.sql.
   Database: PharmacyManagementDB (SQL Server)
*/
USE PharmacyManagementDB;
GO

/*
   One received purchase order creates one auditable stock receipt. The stock
   itself remains in MedicineBatches, BranchStock, and StockTransactions.
*/
IF OBJECT_ID(N'dbo.PurchaseOrderReceipts', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PurchaseOrderReceipts (
        PurchaseOrderReceiptId INT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_PurchaseOrderReceipts PRIMARY KEY,
        PurchaseOrderId INT NOT NULL,
        MedicineId INT NOT NULL,
        BatchId INT NOT NULL,
        BranchId INT NOT NULL,
        BatchNumber NVARCHAR(80) NOT NULL,
        ReceivedQuantity INT NOT NULL,
        PurchasePrice DECIMAL(12,2) NOT NULL,
        SellingPrice DECIMAL(12,2) NOT NULL,
        ReceivedDate DATE NOT NULL,
        ExpiryDate DATE NOT NULL,
        Notes NVARCHAR(500) NULL,
        ReceivedByUserId INT NULL,
        ReceivedAt DATETIME2(0) NOT NULL
            CONSTRAINT DF_PurchaseOrderReceipts_ReceivedAt DEFAULT (SYSDATETIME()),

        CONSTRAINT UQ_PurchaseOrderReceipts_PurchaseOrder UNIQUE (PurchaseOrderId),
        CONSTRAINT CK_PurchaseOrderReceipts_Quantity CHECK (ReceivedQuantity > 0),
        CONSTRAINT CK_PurchaseOrderReceipts_Prices CHECK (PurchasePrice >= 0 AND SellingPrice >= PurchasePrice),
        CONSTRAINT FK_PurchaseOrderReceipts_PurchaseOrder FOREIGN KEY (PurchaseOrderId)
            REFERENCES dbo.PurchaseOrders(PurchaseOrderId) ON DELETE NO ACTION,
        CONSTRAINT FK_PurchaseOrderReceipts_Medicine FOREIGN KEY (MedicineId)
            REFERENCES dbo.Medicines(MedicineId) ON DELETE NO ACTION,
        CONSTRAINT FK_PurchaseOrderReceipts_Batch FOREIGN KEY (BatchId)
            REFERENCES dbo.MedicineBatches(BatchId) ON DELETE NO ACTION,
        CONSTRAINT FK_PurchaseOrderReceipts_Branch FOREIGN KEY (BranchId)
            REFERENCES dbo.PharmacyBranches(BranchId) ON DELETE NO ACTION,
        CONSTRAINT FK_PurchaseOrderReceipts_ReceivedBy FOREIGN KEY (ReceivedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_PurchaseOrderReceipts_ReceivedAt'
      AND object_id = OBJECT_ID(N'dbo.PurchaseOrderReceipts')
)
BEGIN
    CREATE INDEX IX_PurchaseOrderReceipts_ReceivedAt
        ON dbo.PurchaseOrderReceipts(ReceivedAt DESC);
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_PurchaseOrderReceipts_MedicineBranch'
      AND object_id = OBJECT_ID(N'dbo.PurchaseOrderReceipts')
)
BEGIN
    CREATE INDEX IX_PurchaseOrderReceipts_MedicineBranch
        ON dbo.PurchaseOrderReceipts(MedicineId, BranchId);
END;
GO
