/*
   MEDICARE PHARMACY - DELIVERY, OPERATIONS, AND ADMIN CONNECTION
   Run after the Cashier Checkout / Stock-Out migration.
*/
USE PharmacyManagementDB;
GO

/* Keep an audit timeline for every dispatch and delivery status change. */
IF OBJECT_ID(N'dbo.DeliveryEvents', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DeliveryEvents (
        DeliveryEventId INT IDENTITY(1,1) NOT NULL
            CONSTRAINT PK_DeliveryEvents PRIMARY KEY,
        DeliveryId INT NOT NULL,
        EventStatus NVARCHAR(30) NOT NULL,
        EventNote NVARCHAR(500) NULL,
        UpdatedByUserId INT NULL,
        CreatedAt DATETIME2(0) NOT NULL
            CONSTRAINT DF_DeliveryEvents_CreatedAt DEFAULT (SYSDATETIME()),

        CONSTRAINT FK_DeliveryEvents_Delivery FOREIGN KEY (DeliveryId)
            REFERENCES dbo.Deliveries(DeliveryId) ON DELETE NO ACTION,
        CONSTRAINT FK_DeliveryEvents_UpdatedBy FOREIGN KEY (UpdatedByUserId)
            REFERENCES dbo.AppUsers(UserId) ON DELETE NO ACTION
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_DeliveryEvents_DeliveryCreated'
      AND object_id = OBJECT_ID(N'dbo.DeliveryEvents')
)
BEGIN
    CREATE INDEX IX_DeliveryEvents_DeliveryCreated
        ON dbo.DeliveryEvents(DeliveryId, CreatedAt DESC);
END;
GO
