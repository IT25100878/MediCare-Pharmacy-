/*
  Pharmacy Management System - SQL Server database setup
  Run this complete file in SQL Server Management Studio.
  This script is safe to run again: it does not drop existing data.
*/

IF DB_ID(N'PharmacyManagementDB') IS NULL
BEGIN
    CREATE DATABASE PharmacyManagementDB;
END;
GO

USE PharmacyManagementDB;
GO

/* Roles used by Spring Security. */
IF OBJECT_ID(N'dbo.Roles', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Roles (
        RoleId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Roles PRIMARY KEY,
        RoleCode NVARCHAR(50) NOT NULL CONSTRAINT UQ_Roles_RoleCode UNIQUE,
        RoleName NVARCHAR(100) NOT NULL CONSTRAINT UQ_Roles_RoleName UNIQUE
    );
END;
GO

/* Application login accounts. PasswordHash will be created by Spring Boot using BCrypt. */
IF OBJECT_ID(N'dbo.AppUsers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.AppUsers (
        UserId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_AppUsers PRIMARY KEY,
        FullName NVARCHAR(120) NOT NULL,
        Email NVARCHAR(150) NOT NULL CONSTRAINT UQ_AppUsers_Email UNIQUE,
        PasswordHash NVARCHAR(255) NOT NULL,
        RoleId INT NOT NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_AppUsers_IsActive DEFAULT (1),
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_AppUsers_CreatedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT FK_AppUsers_Roles FOREIGN KEY (RoleId)
            REFERENCES dbo.Roles(RoleId)
    );
END;
GO

/* Inventory Supervisor module: medicine and stock records. */
IF OBJECT_ID(N'dbo.Medicines', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Medicines (
        MedicineId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Medicines PRIMARY KEY,
        MedicineName NVARCHAR(150) NOT NULL,
        GenericName NVARCHAR(150) NULL,
        Category NVARCHAR(100) NULL,
        BatchNumber NVARCHAR(80) NOT NULL,
        PurchasePrice DECIMAL(12,2) NOT NULL CONSTRAINT DF_Medicines_PurchasePrice DEFAULT (0),
        SellingPrice DECIMAL(12,2) NOT NULL CONSTRAINT DF_Medicines_SellingPrice DEFAULT (0),
        QuantityInStock INT NOT NULL CONSTRAINT DF_Medicines_Quantity DEFAULT (0),
        ReorderLevel INT NOT NULL CONSTRAINT DF_Medicines_ReorderLevel DEFAULT (0),
        ExpiryDate DATE NOT NULL,
        CreatedByUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Medicines_CreatedAt DEFAULT (SYSDATETIME()),
        UpdatedAt DATETIME2(0) NULL,
        CONSTRAINT UQ_Medicines_Name_Batch UNIQUE (MedicineName, BatchNumber),
        CONSTRAINT CK_Medicines_Prices CHECK (PurchasePrice >= 0 AND SellingPrice >= 0),
        CONSTRAINT CK_Medicines_Stock CHECK (QuantityInStock >= 0 AND ReorderLevel >= 0),
        CONSTRAINT FK_Medicines_CreatedBy FOREIGN KEY (CreatedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

/* Cashier module: customer orders. */
IF OBJECT_ID(N'dbo.CustomerOrders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.CustomerOrders (
        OrderId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CustomerOrders PRIMARY KEY,
        OrderNumber NVARCHAR(30) NOT NULL CONSTRAINT UQ_CustomerOrders_OrderNumber UNIQUE,
        CustomerName NVARCHAR(120) NOT NULL,
        CustomerPhone NVARCHAR(30) NOT NULL,
        OrderDate DATETIME2(0) NOT NULL CONSTRAINT DF_CustomerOrders_OrderDate DEFAULT (SYSDATETIME()),
        TotalAmount DECIMAL(12,2) NOT NULL CONSTRAINT DF_CustomerOrders_TotalAmount DEFAULT (0),
        DiscountAmount DECIMAL(12,2) NOT NULL CONSTRAINT DF_CustomerOrders_DiscountAmount DEFAULT (0),
        PaymentStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_CustomerOrders_PaymentStatus DEFAULT (N'PENDING'),
        OrderStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_CustomerOrders_OrderStatus DEFAULT (N'NEW'),
        CashierUserId INT NULL,
        CONSTRAINT CK_CustomerOrders_Amounts CHECK (TotalAmount >= 0 AND DiscountAmount >= 0),
        CONSTRAINT CK_CustomerOrders_PaymentStatus CHECK (PaymentStatus IN (N'PENDING', N'PAID', N'REFUNDED')),
        CONSTRAINT CK_CustomerOrders_OrderStatus CHECK (OrderStatus IN (N'NEW', N'PROCESSING', N'COMPLETED', N'CANCELLED')),
        CONSTRAINT FK_CustomerOrders_Cashier FOREIGN KEY (CashierUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.OrderItems', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderItems (
        OrderItemId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderItems PRIMARY KEY,
        OrderId INT NOT NULL,
        MedicineId INT NOT NULL,
        Quantity INT NOT NULL,
        UnitPrice DECIMAL(12,2) NOT NULL,
        CONSTRAINT CK_OrderItems_Quantity CHECK (Quantity > 0),
        CONSTRAINT CK_OrderItems_UnitPrice CHECK (UnitPrice >= 0),
        CONSTRAINT FK_OrderItems_Order FOREIGN KEY (OrderId)
            REFERENCES dbo.CustomerOrders(OrderId) ON DELETE CASCADE,
        CONSTRAINT FK_OrderItems_Medicine FOREIGN KEY (MedicineId)
            REFERENCES dbo.Medicines(MedicineId)
    );
END;
GO

/* Duty Pharmacist module: uploaded prescription records and approval status. */
IF OBJECT_ID(N'dbo.Prescriptions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Prescriptions (
        PrescriptionId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Prescriptions PRIMARY KEY,
        PrescriptionNumber NVARCHAR(30) NOT NULL CONSTRAINT UQ_Prescriptions_Number UNIQUE,
        CustomerName NVARCHAR(120) NOT NULL,
        CustomerPhone NVARCHAR(30) NOT NULL,
        PrescriptionFilePath NVARCHAR(500) NULL,
        PrescriptionStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_Prescriptions_Status DEFAULT (N'PENDING'),
        PharmacistUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Prescriptions_CreatedAt DEFAULT (SYSDATETIME()),
        ReviewedAt DATETIME2(0) NULL,
        PharmacistNote NVARCHAR(500) NULL,
        CONSTRAINT CK_Prescriptions_Status CHECK (PrescriptionStatus IN (N'PENDING', N'APPROVED', N'REJECTED')),
        CONSTRAINT FK_Prescriptions_Pharmacist FOREIGN KEY (PharmacistUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

/* Operations Manager module: pharmacy branch records. */
IF OBJECT_ID(N'dbo.PharmacyBranches', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PharmacyBranches (
        BranchId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PharmacyBranches PRIMARY KEY,
        BranchName NVARCHAR(150) NOT NULL CONSTRAINT UQ_PharmacyBranches_Name UNIQUE,
        Address NVARCHAR(300) NOT NULL,
        ContactNumber NVARCHAR(30) NOT NULL,
        ManagerName NVARCHAR(120) NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_PharmacyBranches_IsActive DEFAULT (1),
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_PharmacyBranches_CreatedAt DEFAULT (SYSDATETIME())
    );
END;
GO

/* Procurement Coordinator module: suppliers and purchase orders. */
IF OBJECT_ID(N'dbo.Suppliers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Suppliers (
        SupplierId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Suppliers PRIMARY KEY,
        SupplierName NVARCHAR(150) NOT NULL CONSTRAINT UQ_Suppliers_Name UNIQUE,
        ContactPerson NVARCHAR(120) NULL,
        ContactNumber NVARCHAR(30) NOT NULL,
        Email NVARCHAR(150) NULL,
        Address NVARCHAR(300) NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_Suppliers_IsActive DEFAULT (1),
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Suppliers_CreatedAt DEFAULT (SYSDATETIME())
    );
END;
GO

IF OBJECT_ID(N'dbo.PurchaseOrders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PurchaseOrders (
        PurchaseOrderId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PurchaseOrders PRIMARY KEY,
        PurchaseOrderNumber NVARCHAR(30) NOT NULL CONSTRAINT UQ_PurchaseOrders_Number UNIQUE,
        SupplierId INT NOT NULL,
        OrderDate DATE NOT NULL CONSTRAINT DF_PurchaseOrders_OrderDate DEFAULT (CONVERT(DATE, SYSDATETIME())),
        ExpectedDate DATE NULL,
        TotalAmount DECIMAL(12,2) NOT NULL CONSTRAINT DF_PurchaseOrders_TotalAmount DEFAULT (0),
        PurchaseOrderStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_PurchaseOrders_Status DEFAULT (N'PENDING'),
        ProcurementUserId INT NULL,
        CONSTRAINT CK_PurchaseOrders_Amount CHECK (TotalAmount >= 0),
        CONSTRAINT CK_PurchaseOrders_Status CHECK (PurchaseOrderStatus IN (N'PENDING', N'ORDERED', N'RECEIVED', N'CANCELLED')),
        CONSTRAINT FK_PurchaseOrders_Supplier FOREIGN KEY (SupplierId)
            REFERENCES dbo.Suppliers(SupplierId),
        CONSTRAINT FK_PurchaseOrders_ProcurementUser FOREIGN KEY (ProcurementUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

/* Delivery Management module. */
IF OBJECT_ID(N'dbo.Deliveries', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Deliveries (
        DeliveryId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Deliveries PRIMARY KEY,
        DeliveryNumber NVARCHAR(30) NOT NULL CONSTRAINT UQ_Deliveries_Number UNIQUE,
        OrderId INT NULL,
        CustomerName NVARCHAR(120) NOT NULL,
        CustomerPhone NVARCHAR(30) NOT NULL,
        DeliveryAddress NVARCHAR(300) NOT NULL,
        DeliveryPersonName NVARCHAR(120) NULL,
        DeliveryDate DATE NULL,
        DeliveryStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_Deliveries_Status DEFAULT (N'PENDING'),
        DeliveryCoordinatorUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Deliveries_CreatedAt DEFAULT (SYSDATETIME()),
        CONSTRAINT CK_Deliveries_Status CHECK (DeliveryStatus IN (N'PENDING', N'ASSIGNED', N'OUT_FOR_DELIVERY', N'DELIVERED', N'CANCELLED')),
        CONSTRAINT FK_Deliveries_Order FOREIGN KEY (OrderId)
            REFERENCES dbo.CustomerOrders(OrderId) ON DELETE SET NULL,
        CONSTRAINT FK_Deliveries_Coordinator FOREIGN KEY (DeliveryCoordinatorUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE SET NULL
    );
END;
GO

/* Feedback button appears on every role dashboard. */
IF OBJECT_ID(N'dbo.Feedback', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Feedback (
        FeedbackId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Feedback PRIMARY KEY,
        UserId INT NOT NULL,
        Subject NVARCHAR(150) NOT NULL,
        Message NVARCHAR(MAX) NOT NULL,
        Rating TINYINT NULL,
        FeedbackStatus NVARCHAR(20) NOT NULL CONSTRAINT DF_Feedback_Status DEFAULT (N'NEW'),
        AdminResponse NVARCHAR(MAX) NULL,
        CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Feedback_CreatedAt DEFAULT (SYSDATETIME()),
        UpdatedAt DATETIME2(0) NULL,
        CONSTRAINT CK_Feedback_Rating CHECK (Rating IS NULL OR Rating BETWEEN 1 AND 5),
        CONSTRAINT CK_Feedback_Status CHECK (FeedbackStatus IN (N'NEW', N'REVIEWED', N'RESOLVED')),
        CONSTRAINT FK_Feedback_User FOREIGN KEY (UserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE CASCADE
    );
END;
GO

/* Insert the seven required roles once. */
INSERT INTO dbo.Roles (RoleCode, RoleName)
SELECT v.RoleCode, v.RoleName
FROM (VALUES
    (N'ADMIN', N'Admin'),
    (N'INVENTORY_SUPERVISOR', N'Inventory Supervisor'),
    (N'CASHIER', N'Cashier'),
    (N'DUTY_PHARMACIST', N'Duty Pharmacist'),
    (N'OPERATIONS_MANAGER', N'Operations Manager'),
    (N'PROCUREMENT_COORDINATOR', N'Procurement Coordinator'),
    (N'CUSTOMER_FEEDBACK_MANAGER', N'Customer Feedback Manager')
) AS v(RoleCode, RoleName)
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.Roles AS r
    WHERE r.RoleCode = v.RoleCode
);
GO

/* Quick verification after running the script. */
SELECT RoleId, RoleCode, RoleName
FROM dbo.Roles
ORDER BY RoleId;

SELECT name AS TableName
FROM sys.tables
WHERE schema_id = SCHEMA_ID(N'dbo')
ORDER BY name;
GO
