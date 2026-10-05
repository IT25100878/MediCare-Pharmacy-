USE PharmacyManagementDB;
GO

SELECT OrderId, OrderNumber, CustomerName, TotalAmount, DiscountAmount, PaymentStatus, OrderStatus
FROM dbo.CustomerOrders
ORDER BY OrderId DESC;

SELECT PrescriptionId, PrescriptionNumber, CustomerName, PrescriptionStatus, ReviewedAt
FROM dbo.Prescriptions
ORDER BY PrescriptionId DESC;

SELECT BranchId, BranchName, ContactNumber, IsActive
FROM dbo.PharmacyBranches
ORDER BY BranchId DESC;

SELECT SupplierId, SupplierName, ContactNumber, IsActive
FROM dbo.Suppliers
ORDER BY SupplierId DESC;

SELECT PurchaseOrderId, PurchaseOrderNumber, SupplierId, TotalAmount, PurchaseOrderStatus
FROM dbo.PurchaseOrders
ORDER BY PurchaseOrderId DESC;

SELECT DeliveryId, DeliveryNumber, CustomerName, DeliveryStatus, DeliveryDate
FROM dbo.Deliveries
ORDER BY DeliveryId DESC;

SELECT UserId, FullName, Email, RoleId, IsActive
FROM dbo.AppUsers
ORDER BY UserId DESC;

SELECT FeedbackId, UserId, Subject, Rating, FeedbackStatus, AdminResponse
FROM dbo.Feedback
ORDER BY FeedbackId DESC;
GO
