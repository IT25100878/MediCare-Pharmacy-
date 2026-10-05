/*
   MediCare Pharmacy Management System
   Promotion CRUD + permanent supplier delete fix

   Run this script after 00_base_schema.sql through 05_professional_portal_upgrade.sql.
   It is safe to run more than once.
*/
USE PharmacyManagementDB;
GO

/* Create the campaign table if the earlier professional portal upgrade was not run. */
IF OBJECT_ID(N'dbo.Promotions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Promotions (
        PromotionId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Promotions PRIMARY KEY,
        CampaignCode NVARCHAR(40) NOT NULL CONSTRAINT UQ_Promotions_CampaignCode UNIQUE,
        CampaignName NVARCHAR(150) NOT NULL,
        Description NVARCHAR(500) NULL,
        DiscountType NVARCHAR(20) NOT NULL,
        DiscountValue DECIMAL(12,2) NOT NULL,
        MinimumOrderAmount DECIMAL(12,2) NOT NULL CONSTRAINT DF_Promotions_MinOrder DEFAULT (0),
        MedicineId INT NULL,
        Category NVARCHAR(100) NULL,
        BranchId INT NULL,
        StartDate DATE NOT NULL,
        EndDate DATE NOT NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_Promotions_IsActive DEFAULT (1),
        IsArchived BIT NOT NULL CONSTRAINT DF_Promotions_IsArchived DEFAULT (0),
        CreatedByUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Promotions_CreatedAt DEFAULT (SYSDATETIME()),
        UpdatedAt DATETIME2(0) NULL,
        CONSTRAINT CK_Promotions_Type CHECK (DiscountType IN (N'PERCENT', N'FIXED')),
        CONSTRAINT CK_Promotions_Value CHECK (DiscountValue > 0),
        CONSTRAINT CK_Promotions_Minimum CHECK (MinimumOrderAmount >= 0),
        CONSTRAINT CK_Promotions_Dates CHECK (EndDate >= StartDate),
        CONSTRAINT FK_Promotions_Medicine FOREIGN KEY (MedicineId)
            REFERENCES dbo.Medicines(MedicineId) ON DELETE NO ACTION,
        CONSTRAINT FK_Promotions_Branch FOREIGN KEY (BranchId)
            REFERENCES dbo.PharmacyBranches(BranchId) ON DELETE NO ACTION,
        CONSTRAINT FK_Promotions_CreatedBy FOREIGN KEY (CreatedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION
    );
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'IX_Promotions_ActiveDates'
      AND object_id = OBJECT_ID(N'dbo.Promotions')
)
    CREATE INDEX IX_Promotions_ActiveDates
        ON dbo.Promotions(IsActive, IsArchived, StartDate, EndDate);
GO

/* Checkout needs a place to record the campaign used for each order. */
IF OBJECT_ID(N'dbo.PromotionRedemptions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PromotionRedemptions (
        PromotionRedemptionId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PromotionRedemptions PRIMARY KEY,
        PromotionId INT NOT NULL,
        OrderId INT NOT NULL CONSTRAINT UQ_PromotionRedemptions_Order UNIQUE,
        DiscountAmount DECIMAL(12,2) NOT NULL,
        RedeemedAt DATETIME2(0) NOT NULL CONSTRAINT DF_PromotionRedemptions_RedeemedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT CK_PromotionRedemptions_Amount CHECK (DiscountAmount > 0),
        CONSTRAINT FK_PromotionRedemptions_Promotion FOREIGN KEY (PromotionId)
            REFERENCES dbo.Promotions(PromotionId) ON DELETE NO ACTION,
        CONSTRAINT FK_PromotionRedemptions_Order FOREIGN KEY (OrderId)
            REFERENCES dbo.CustomerOrders(OrderId) ON DELETE NO ACTION
    );
END;
GO

/*
   Supplier profile deletion is permanent. Purchase orders remain for audit,
   but their SupplierId becomes NULL when that supplier is removed.
*/
IF OBJECT_ID(N'dbo.PurchaseOrders', N'U') IS NOT NULL
   AND OBJECT_ID(N'dbo.Suppliers', N'U') IS NOT NULL
BEGIN
    DECLARE @supplierForeignKey SYSNAME;
    DECLARE @sql NVARCHAR(MAX);

    SELECT TOP (1) @supplierForeignKey = fk.name
    FROM sys.foreign_keys fk
    WHERE fk.parent_object_id = OBJECT_ID(N'dbo.PurchaseOrders')
      AND fk.referenced_object_id = OBJECT_ID(N'dbo.Suppliers');

    IF @supplierForeignKey IS NOT NULL
    BEGIN
        SET @sql = N'ALTER TABLE dbo.PurchaseOrders DROP CONSTRAINT '
            + QUOTENAME(@supplierForeignKey) + N';';
        EXEC sys.sp_executesql @sql;
    END;

    ALTER TABLE dbo.PurchaseOrders ALTER COLUMN SupplierId INT NULL;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys fk
        WHERE fk.parent_object_id = OBJECT_ID(N'dbo.PurchaseOrders')
          AND fk.referenced_object_id = OBJECT_ID(N'dbo.Suppliers')
    )
        ALTER TABLE dbo.PurchaseOrders
            ADD CONSTRAINT FK_PurchaseOrders_Supplier
            FOREIGN KEY (SupplierId)
            REFERENCES dbo.Suppliers(SupplierId) ON DELETE SET NULL;
END;
GO

SELECT name AS TableName
FROM sys.tables
WHERE name IN (N'Promotions', N'PromotionRedemptions')
ORDER BY name;
GO
