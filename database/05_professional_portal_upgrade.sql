/*
   MEDICARE FOR ALL - PROFESSIONAL PORTAL UPGRADE
   Run this file after 04_delivery_operations_admin.sql.

   This migration keeps every previous record.  It changes the seventh staff
   workspace from a stand-alone Delivery Management role to Customer Feedback
   Manager, while Delivery remains an Operations Manager workflow.
*/
USE PharmacyManagementDB;
GO

/* Convert the previously created seventh role without breaking existing users. */
IF EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = N'DELIVERY_MANAGEMENT')
   AND NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = N'CUSTOMER_FEEDBACK_MANAGER')
BEGIN
    UPDATE dbo.Roles
    SET RoleCode = N'CUSTOMER_FEEDBACK_MANAGER',
        RoleName = N'Customer Feedback Manager'
    WHERE RoleCode = N'DELIVERY_MANAGEMENT';
END;
GO

IF EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = N'DELIVERY_MANAGEMENT')
   AND EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = N'CUSTOMER_FEEDBACK_MANAGER')
BEGIN
    UPDATE dbo.AppUsers
    SET RoleId = (SELECT RoleId FROM dbo.Roles WHERE RoleCode = N'CUSTOMER_FEEDBACK_MANAGER')
    WHERE RoleId = (SELECT RoleId FROM dbo.Roles WHERE RoleCode = N'DELIVERY_MANAGEMENT');

    DELETE FROM dbo.Roles WHERE RoleCode = N'DELIVERY_MANAGEMENT';
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Roles WHERE RoleCode = N'CUSTOMER_FEEDBACK_MANAGER')
BEGIN
    INSERT INTO dbo.Roles (RoleCode, RoleName)
    VALUES (N'CUSTOMER_FEEDBACK_MANAGER', N'Customer Feedback Manager');
END;
GO

/* Feedback is now a complete case-management workflow, not a delete-only inbox. */
IF COL_LENGTH(N'dbo.Feedback', N'CustomerName') IS NULL
    ALTER TABLE dbo.Feedback ADD CustomerName NVARCHAR(120) NULL;
GO
IF COL_LENGTH(N'dbo.Feedback', N'CustomerPhone') IS NULL
    ALTER TABLE dbo.Feedback ADD CustomerPhone NVARCHAR(30) NULL;
GO
IF COL_LENGTH(N'dbo.Feedback', N'Category') IS NULL
    ALTER TABLE dbo.Feedback ADD Category NVARCHAR(40) NOT NULL
        CONSTRAINT DF_Feedback_Category DEFAULT (N'GENERAL') WITH VALUES;
GO
IF COL_LENGTH(N'dbo.Feedback', N'Priority') IS NULL
    ALTER TABLE dbo.Feedback ADD Priority NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Feedback_Priority DEFAULT (N'MEDIUM') WITH VALUES;
GO
IF COL_LENGTH(N'dbo.Feedback', N'AssignedToUserId') IS NULL
    ALTER TABLE dbo.Feedback ADD AssignedToUserId INT NULL;
GO
IF COL_LENGTH(N'dbo.Feedback', N'IsArchived') IS NULL
    ALTER TABLE dbo.Feedback ADD IsArchived BIT NOT NULL
        CONSTRAINT DF_Feedback_IsArchived DEFAULT (0) WITH VALUES;
GO
IF COL_LENGTH(N'dbo.Feedback', N'ClosedAt') IS NULL
    ALTER TABLE dbo.Feedback ADD ClosedAt DATETIME2(0) NULL;
GO

/* Public customers may send feedback, so UserId is now optional. */
ALTER TABLE dbo.Feedback ALTER COLUMN UserId INT NULL;
GO

UPDATE dbo.Feedback
SET FeedbackStatus = N'IN_PROGRESS'
WHERE FeedbackStatus = N'REVIEWED';
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Feedback_Status')
    ALTER TABLE dbo.Feedback DROP CONSTRAINT CK_Feedback_Status;
GO
ALTER TABLE dbo.Feedback
    ADD CONSTRAINT CK_Feedback_Status
    CHECK (FeedbackStatus IN (N'NEW', N'IN_PROGRESS', N'RESOLVED', N'CLOSED'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Feedback_Category')
    ALTER TABLE dbo.Feedback ADD CONSTRAINT CK_Feedback_Category
    CHECK (Category IN (N'GENERAL', N'SERVICE', N'ORDER', N'PRESCRIPTION', N'INVENTORY', N'COMPLAINT'));
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_Feedback_Priority')
    ALTER TABLE dbo.Feedback ADD CONSTRAINT CK_Feedback_Priority
    CHECK (Priority IN (N'LOW', N'MEDIUM', N'HIGH', N'URGENT'));
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_Feedback_AssignedTo')
    ALTER TABLE dbo.Feedback ADD CONSTRAINT FK_Feedback_AssignedTo
    FOREIGN KEY (AssignedToUserId) REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Feedback_Queue' AND object_id = OBJECT_ID(N'dbo.Feedback'))
    CREATE INDEX IX_Feedback_Queue ON dbo.Feedback(IsArchived, FeedbackStatus, Priority, CreatedAt DESC);
GO

/* Prescription files are retained for audit; removal from the review queue is an archive action. */
IF COL_LENGTH(N'dbo.Prescriptions', N'IsArchived') IS NULL
    ALTER TABLE dbo.Prescriptions ADD IsArchived BIT NOT NULL
        CONSTRAINT DF_Prescriptions_IsArchived DEFAULT (0) WITH VALUES;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Prescriptions_ReviewQueue' AND object_id = OBJECT_ID(N'dbo.Prescriptions'))
    CREATE INDEX IX_Prescriptions_ReviewQueue ON dbo.Prescriptions(IsArchived, PrescriptionStatus, CreatedAt DESC);
GO

/* Supplier profiles include the business data needed for safe procurement decisions. */
IF COL_LENGTH(N'dbo.Suppliers', N'BusinessRegistrationId') IS NULL
    ALTER TABLE dbo.Suppliers ADD BusinessRegistrationId NVARCHAR(80) NULL;
GO
IF COL_LENGTH(N'dbo.Suppliers', N'MedicineCategory') IS NULL
    ALTER TABLE dbo.Suppliers ADD MedicineCategory NVARCHAR(100) NULL;
GO
IF COL_LENGTH(N'dbo.Suppliers', N'PaymentTerms') IS NULL
    ALTER TABLE dbo.Suppliers ADD PaymentTerms NVARCHAR(150) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_Suppliers_BusinessRegistrationId' AND object_id = OBJECT_ID(N'dbo.Suppliers'))
    CREATE UNIQUE INDEX UX_Suppliers_BusinessRegistrationId ON dbo.Suppliers(BusinessRegistrationId)
    WHERE BusinessRegistrationId IS NOT NULL;
GO

/* Operations Manager campaigns apply automatically during cashier checkout. */
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
        CONSTRAINT FK_Promotions_Medicine FOREIGN KEY (MedicineId) REFERENCES dbo.Medicines(MedicineId) ON DELETE NO ACTION,
        CONSTRAINT FK_Promotions_Branch FOREIGN KEY (BranchId) REFERENCES dbo.PharmacyBranches(BranchId) ON DELETE NO ACTION,
        CONSTRAINT FK_Promotions_CreatedBy FOREIGN KEY (CreatedByUserId) REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION
    );
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_Promotions_ActiveDates' AND object_id = OBJECT_ID(N'dbo.Promotions'))
    CREATE INDEX IX_Promotions_ActiveDates ON dbo.Promotions(IsActive, IsArchived, StartDate, EndDate);
GO

/* Invoices are immutable sale documents.  A void keeps the audit trail. */
IF OBJECT_ID(N'dbo.Invoices', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Invoices (
        InvoiceId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Invoices PRIMARY KEY,
        InvoiceNumber NVARCHAR(40) NOT NULL CONSTRAINT UQ_Invoices_InvoiceNumber UNIQUE,
        OrderId INT NOT NULL CONSTRAINT UQ_Invoices_OrderId UNIQUE,
        InvoiceStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_Invoices_Status DEFAULT (N'ISSUED'),
        GeneratedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Invoices_GeneratedAt DEFAULT (SYSDATETIME()),
        PrintedAt DATETIME2(0) NULL,
        VoidedAt DATETIME2(0) NULL,
        Notes NVARCHAR(500) NULL,
        CONSTRAINT CK_Invoices_Status CHECK (InvoiceStatus IN (N'ISSUED', N'VOID')),
        CONSTRAINT FK_Invoices_Order FOREIGN KEY (OrderId) REFERENCES dbo.CustomerOrders(OrderId) ON DELETE NO ACTION
    );
END;
GO

IF OBJECT_ID(N'dbo.PromotionRedemptions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PromotionRedemptions (
        PromotionRedemptionId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PromotionRedemptions PRIMARY KEY,
        PromotionId INT NOT NULL,
        OrderId INT NOT NULL CONSTRAINT UQ_PromotionRedemptions_Order UNIQUE,
        DiscountAmount DECIMAL(12,2) NOT NULL,
        RedeemedAt DATETIME2(0) NOT NULL CONSTRAINT DF_PromotionRedemptions_RedeemedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT CK_PromotionRedemptions_Amount CHECK (DiscountAmount > 0),
        CONSTRAINT FK_PromotionRedemptions_Promotion FOREIGN KEY (PromotionId) REFERENCES dbo.Promotions(PromotionId) ON DELETE NO ACTION,
        CONSTRAINT FK_PromotionRedemptions_Order FOREIGN KEY (OrderId) REFERENCES dbo.CustomerOrders(OrderId) ON DELETE NO ACTION
    );
END;
GO

/* Helpful dashboard verification after the upgrade. */
SELECT RoleCode, RoleName FROM dbo.Roles ORDER BY RoleId;
SELECT name AS TableName FROM sys.tables WHERE name IN (N'Promotions', N'Invoices', N'PromotionRedemptions') ORDER BY name;
GO
