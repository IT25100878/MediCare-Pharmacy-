/*
  MediCare Pharmacy - Inventory Connected Workflow Migration
  SQL Server / PharmacyManagementDB

  Run this file ONCE in SSMS after PharmacyManagementDB_Setup.sql.
  It is designed to be safe to run again and does not delete existing records.
*/

USE PharmacyManagementDB;
GO

/* A location is needed before stock can be tracked per branch. */
IF NOT EXISTS (SELECT 1 FROM dbo.PharmacyBranches WHERE BranchName = N'Central Warehouse')
BEGIN
    INSERT INTO dbo.PharmacyBranches (BranchName, Address, ContactNumber, ManagerName, IsActive)
    VALUES (N'Central Warehouse', N'MediCare Central Stock Location', N'0000000000', N'System', 1);
END;
GO

/* One medicine can have several supplier batches with different expiry dates. */
IF OBJECT_ID(N'dbo.MedicineBatches', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.MedicineBatches (
        BatchId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MedicineBatches PRIMARY KEY,
        MedicineId INT NOT NULL,
        BatchNumber NVARCHAR(80) NOT NULL,
        PurchasePrice DECIMAL(12,2) NOT NULL,
        SellingPrice DECIMAL(12,2) NOT NULL,
        InitialQuantity INT NOT NULL,
        AvailableQuantity INT NOT NULL,
        ReceivedDate DATE NOT NULL CONSTRAINT DF_MedicineBatches_ReceivedDate DEFAULT (CONVERT(DATE, SYSDATETIME())),
        ExpiryDate DATE NOT NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_MedicineBatches_IsActive DEFAULT (1),
        CreatedByUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_MedicineBatches_CreatedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT UQ_MedicineBatches_Medicine_Batch UNIQUE (MedicineId, BatchNumber),
        CONSTRAINT CK_MedicineBatches_Prices CHECK (PurchasePrice >= 0 AND SellingPrice >= 0),
        CONSTRAINT CK_MedicineBatches_Quantities CHECK (InitialQuantity >= 0 AND AvailableQuantity >= 0),
        CONSTRAINT FK_MedicineBatches_Medicine FOREIGN KEY (MedicineId)
            REFERENCES dbo.Medicines(MedicineId),
        CONSTRAINT FK_MedicineBatches_CreatedBy FOREIGN KEY (CreatedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

/* This table holds the exact quantity of each batch at each branch. */
IF OBJECT_ID(N'dbo.BranchStock', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BranchStock (
        BranchStockId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_BranchStock PRIMARY KEY,
        BranchId INT NOT NULL,
        BatchId INT NOT NULL,
        QuantityInStock INT NOT NULL CONSTRAINT DF_BranchStock_Quantity DEFAULT (0),
        UpdatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_BranchStock_UpdatedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT UQ_BranchStock_Branch_Batch UNIQUE (BranchId, BatchId),
        CONSTRAINT CK_BranchStock_Quantity CHECK (QuantityInStock >= 0),
        CONSTRAINT FK_BranchStock_Branch FOREIGN KEY (BranchId)
            REFERENCES dbo.PharmacyBranches(BranchId),
        CONSTRAINT FK_BranchStock_Batch FOREIGN KEY (BatchId)
            REFERENCES dbo.MedicineBatches(BatchId)
    );
END;
GO

/* Every stock change is recorded; this makes stock movement auditable. */
IF OBJECT_ID(N'dbo.StockTransactions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.StockTransactions (
        StockTransactionId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_StockTransactions PRIMARY KEY,
        BatchId INT NOT NULL,
        BranchId INT NOT NULL,
        TransactionType NVARCHAR(30) NOT NULL,
        QuantityChange INT NOT NULL,
        ReferenceType NVARCHAR(40) NULL,
        ReferenceId INT NULL,
        Notes NVARCHAR(500) NULL,
        CreatedByUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_StockTransactions_CreatedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT CK_StockTransactions_NonZero CHECK (QuantityChange <> 0),
        CONSTRAINT CK_StockTransactions_Type CHECK (TransactionType IN (
            N'OPENING_BALANCE', N'STOCK_IN', N'SALE_OUT',
            N'TRANSFER_OUT', N'TRANSFER_IN',
            N'ADJUSTMENT_ADD', N'ADJUSTMENT_REMOVE'
        )),
        CONSTRAINT FK_StockTransactions_Batch FOREIGN KEY (BatchId)
            REFERENCES dbo.MedicineBatches(BatchId),
        CONSTRAINT FK_StockTransactions_Branch FOREIGN KEY (BranchId)
            REFERENCES dbo.PharmacyBranches(BranchId),
        CONSTRAINT FK_StockTransactions_CreatedBy FOREIGN KEY (CreatedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

/* A transfer is requested first, then dispatched and received. */
IF OBJECT_ID(N'dbo.StockTransfers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.StockTransfers (
        StockTransferId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_StockTransfers PRIMARY KEY,
        TransferNumber NVARCHAR(35) NOT NULL CONSTRAINT UQ_StockTransfers_Number UNIQUE,
        BatchId INT NOT NULL,
        FromBranchId INT NOT NULL,
        ToBranchId INT NOT NULL,
        Quantity INT NOT NULL,
        TransferStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_StockTransfers_Status DEFAULT (N'PENDING'),
        RequestedByUserId INT NULL,
        DispatchedByUserId INT NULL,
        ReceivedByUserId INT NULL,
        RequestedAt DATETIME2(0) NOT NULL CONSTRAINT DF_StockTransfers_RequestedAt DEFAULT (SYSDATETIME()),
        DispatchedAt DATETIME2(0) NULL,
        ReceivedAt DATETIME2(0) NULL,
        Notes NVARCHAR(500) NULL,
        CONSTRAINT CK_StockTransfers_Quantity CHECK (Quantity > 0),
        CONSTRAINT CK_StockTransfers_DifferentBranches CHECK (FromBranchId <> ToBranchId),
        CONSTRAINT CK_StockTransfers_Status CHECK (TransferStatus IN (N'PENDING', N'IN_TRANSIT', N'COMPLETED', N'CANCELLED')),
        CONSTRAINT FK_StockTransfers_Batch FOREIGN KEY (BatchId)
            REFERENCES dbo.MedicineBatches(BatchId),
        CONSTRAINT FK_StockTransfers_FromBranch FOREIGN KEY (FromBranchId)
            REFERENCES dbo.PharmacyBranches(BranchId),
        CONSTRAINT FK_StockTransfers_ToBranch FOREIGN KEY (ToBranchId)
            REFERENCES dbo.PharmacyBranches(BranchId),
        /*
           SQL Server does not allow three cascading paths from AppUsers to
           StockTransfers. Keep transfer audit history intact and prevent a
           referenced user from being deleted; deactivate users instead.
        */
        CONSTRAINT FK_StockTransfers_RequestedBy FOREIGN KEY (RequestedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION,
        CONSTRAINT FK_StockTransfers_DispatchedBy FOREIGN KEY (DispatchedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION,
        CONSTRAINT FK_StockTransfers_ReceivedBy FOREIGN KEY (ReceivedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION
    );
END;
GO

/* Helpful indexes for inventory dashboards and alert queries. */
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_MedicineBatches_Medicine_Expiry' AND object_id = OBJECT_ID(N'dbo.MedicineBatches'))
    CREATE INDEX IX_MedicineBatches_Medicine_Expiry ON dbo.MedicineBatches (MedicineId, ExpiryDate);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_BranchStock_Batch' AND object_id = OBJECT_ID(N'dbo.BranchStock'))
    CREATE INDEX IX_BranchStock_Batch ON dbo.BranchStock (BatchId);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_StockTransactions_Batch_CreatedAt' AND object_id = OBJECT_ID(N'dbo.StockTransactions'))
    CREATE INDEX IX_StockTransactions_Batch_CreatedAt ON dbo.StockTransactions (BatchId, CreatedAt DESC);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_StockTransfers_Status' AND object_id = OBJECT_ID(N'dbo.StockTransfers'))
    CREATE INDEX IX_StockTransfers_Status ON dbo.StockTransfers (TransferStatus, RequestedAt DESC);
GO

/* Convert old Medicine rows into an opening batch at Central Warehouse once. */
DECLARE @CentralWarehouseId INT = (
    SELECT TOP (1) BranchId
    FROM dbo.PharmacyBranches
    WHERE BranchName = N'Central Warehouse'
);

INSERT INTO dbo.MedicineBatches (
    MedicineId, BatchNumber, PurchasePrice, SellingPrice,
    InitialQuantity, AvailableQuantity, ReceivedDate, ExpiryDate, CreatedByUserId
)
SELECT
    m.MedicineId, m.BatchNumber, m.PurchasePrice, m.SellingPrice,
    m.QuantityInStock, m.QuantityInStock, CONVERT(DATE, m.CreatedAt), m.ExpiryDate, m.CreatedByUserId
FROM dbo.Medicines AS m
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.MedicineBatches AS b
    WHERE b.MedicineId = m.MedicineId AND b.BatchNumber = m.BatchNumber
);

INSERT INTO dbo.BranchStock (BranchId, BatchId, QuantityInStock)
SELECT @CentralWarehouseId, b.BatchId, b.AvailableQuantity
FROM dbo.MedicineBatches AS b
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.BranchStock AS bs
    WHERE bs.BatchId = b.BatchId
);

INSERT INTO dbo.StockTransactions (BatchId, BranchId, TransactionType, QuantityChange, ReferenceType, Notes, CreatedByUserId)
SELECT b.BatchId, @CentralWarehouseId, N'OPENING_BALANCE', b.AvailableQuantity, N'MIGRATION', N'Imported from existing Medicines stock.', b.CreatedByUserId
FROM dbo.MedicineBatches AS b
WHERE b.AvailableQuantity > 0
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.StockTransactions AS t
      WHERE t.BatchId = b.BatchId AND t.ReferenceType = N'MIGRATION'
  );

/* Keep the legacy Medicines quantity equal to the sum of live branch stock. */
UPDATE m
SET QuantityInStock = ISNULL(summary.TotalQuantity, 0),
    UpdatedAt = SYSDATETIME()
FROM dbo.Medicines AS m
OUTER APPLY (
    SELECT SUM(bs.QuantityInStock) AS TotalQuantity
    FROM dbo.MedicineBatches AS b
    INNER JOIN dbo.BranchStock AS bs ON bs.BatchId = b.BatchId
    WHERE b.MedicineId = m.MedicineId
) AS summary;
GO

/* Quick verification */
SELECT N'MedicineBatches' AS TableName, COUNT(*) AS RecordCount FROM dbo.MedicineBatches
UNION ALL SELECT N'BranchStock', COUNT(*) FROM dbo.BranchStock
UNION ALL SELECT N'StockTransactions', COUNT(*) FROM dbo.StockTransactions
UNION ALL SELECT N'StockTransfers', COUNT(*) FROM dbo.StockTransfers;
GO
