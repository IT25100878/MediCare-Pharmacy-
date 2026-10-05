/*
   MediCare Pharmacy Management System
   Permanent delete workflow for Inventory and Procurement campaigns

   Run this file once in SSMS after database/00_base_schema.sql through
   database/06_promotion_supplier_crud_fix.sql.

   What this enables
   - Inventory Supervisor: permanently deletes one medicine and its direct
     stock, receipt, order-item, transfer, campaign, and campaign-redemption rows.
   - Procurement Coordinator / Operations Manager: permanently deletes one
     promotion campaign and its redemption rows.

   Customer order headers, invoices, and other records not owned exclusively
   by the medicine are retained. This prevents one old medicine from deleting
   an entire customer order that may include other medicines.
*/
USE PharmacyManagementDB;
GO

/*
   Repair the supplier foreign-key change if a previous version of script 06
   stopped near QUOTENAME. This makes supplier deletion permanent while its
   old purchase orders remain with SupplierId = NULL.
*/
IF OBJECT_ID(N'dbo.PurchaseOrders', N'U') IS NOT NULL
   AND OBJECT_ID(N'dbo.Suppliers', N'U') IS NOT NULL
BEGIN
    DECLARE @supplierForeignKey SYSNAME;
    DECLARE @supplierSql NVARCHAR(MAX);

    SELECT TOP (1) @supplierForeignKey = fk.name
    FROM sys.foreign_keys AS fk
    WHERE fk.parent_object_id = OBJECT_ID(N'dbo.PurchaseOrders')
      AND fk.referenced_object_id = OBJECT_ID(N'dbo.Suppliers');

    IF @supplierForeignKey IS NOT NULL
    BEGIN
        SET @supplierSql = N'ALTER TABLE dbo.PurchaseOrders DROP CONSTRAINT '
            + QUOTENAME(@supplierForeignKey) + N';';
        EXEC sys.sp_executesql @supplierSql;
    END;

    ALTER TABLE dbo.PurchaseOrders ALTER COLUMN SupplierId INT NULL;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys AS fk
        WHERE fk.parent_object_id = OBJECT_ID(N'dbo.PurchaseOrders')
          AND fk.referenced_object_id = OBJECT_ID(N'dbo.Suppliers')
    )
    BEGIN
        ALTER TABLE dbo.PurchaseOrders
            ADD CONSTRAINT FK_PurchaseOrders_Supplier
            FOREIGN KEY (SupplierId)
            REFERENCES dbo.Suppliers(SupplierId) ON DELETE SET NULL;
    END;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_DeletePromotionPermanently
    @PromotionId INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM dbo.Promotions WHERE PromotionId = @PromotionId)
            THROW 50021, 'Promotion campaign was not found.', 1;

        IF OBJECT_ID(N'dbo.PromotionRedemptions', N'U') IS NOT NULL
        BEGIN
            DELETE FROM dbo.PromotionRedemptions
            WHERE PromotionId = @PromotionId;
        END;

        DELETE FROM dbo.Promotions
        WHERE PromotionId = @PromotionId;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_DeleteMedicinePermanently
    @MedicineId INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM dbo.Medicines WHERE MedicineId = @MedicineId)
            THROW 50022, 'Medicine record was not found.', 1;

        /* A targeted campaign is owned by the medicine when the medicine is removed. */
        IF OBJECT_ID(N'dbo.PromotionRedemptions', N'U') IS NOT NULL
           AND OBJECT_ID(N'dbo.Promotions', N'U') IS NOT NULL
        BEGIN
            DELETE redemption
            FROM dbo.PromotionRedemptions AS redemption
            INNER JOIN dbo.Promotions AS promotion
                ON promotion.PromotionId = redemption.PromotionId
            WHERE promotion.MedicineId = @MedicineId;
        END;

        IF OBJECT_ID(N'dbo.Promotions', N'U') IS NOT NULL
        BEGIN
            DELETE FROM dbo.Promotions
            WHERE MedicineId = @MedicineId;
        END;

        /* Remove direct transaction rows before their batch/medicine parent rows. */
        IF OBJECT_ID(N'dbo.PurchaseOrderReceipts', N'U') IS NOT NULL
        BEGIN
            DELETE receipt
            FROM dbo.PurchaseOrderReceipts AS receipt
            LEFT JOIN dbo.MedicineBatches AS batch
                ON batch.BatchId = receipt.BatchId
            WHERE receipt.MedicineId = @MedicineId
               OR batch.MedicineId = @MedicineId;
        END;

        IF OBJECT_ID(N'dbo.OrderItems', N'U') IS NOT NULL
        BEGIN
            DELETE item
            FROM dbo.OrderItems AS item
            LEFT JOIN dbo.MedicineBatches AS batch
                ON batch.BatchId = item.BatchId
            WHERE item.MedicineId = @MedicineId
               OR batch.MedicineId = @MedicineId;
        END;

        IF OBJECT_ID(N'dbo.BranchStock', N'U') IS NOT NULL
        BEGIN
            DELETE stock
            FROM dbo.BranchStock AS stock
            INNER JOIN dbo.MedicineBatches AS batch
                ON batch.BatchId = stock.BatchId
            WHERE batch.MedicineId = @MedicineId;
        END;

        IF OBJECT_ID(N'dbo.StockTransfers', N'U') IS NOT NULL
        BEGIN
            DELETE transferRecord
            FROM dbo.StockTransfers AS transferRecord
            INNER JOIN dbo.MedicineBatches AS batch
                ON batch.BatchId = transferRecord.BatchId
            WHERE batch.MedicineId = @MedicineId;
        END;

        IF OBJECT_ID(N'dbo.StockTransactions', N'U') IS NOT NULL
        BEGIN
            DELETE transactionRecord
            FROM dbo.StockTransactions AS transactionRecord
            INNER JOIN dbo.MedicineBatches AS batch
                ON batch.BatchId = transactionRecord.BatchId
            WHERE batch.MedicineId = @MedicineId;
        END;

        /* Optional Part-F audit table is also cleared when it exists. */
        IF OBJECT_ID(N'dbo.MedicineAuditLog', N'U') IS NOT NULL
        BEGIN
            DELETE FROM dbo.MedicineAuditLog
            WHERE MedicineId = @MedicineId;
        END;

        IF OBJECT_ID(N'dbo.MedicineBatches', N'U') IS NOT NULL
        BEGIN
            DELETE FROM dbo.MedicineBatches
            WHERE MedicineId = @MedicineId;
        END;

        DELETE FROM dbo.Medicines
        WHERE MedicineId = @MedicineId;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

/* Verification only: confirms that the required delete procedures are ready. */
SELECT name AS PermanentDeleteProcedure
FROM sys.procedures
WHERE name IN (N'usp_DeleteMedicinePermanently', N'usp_DeletePromotionPermanently')
ORDER BY name;
GO
