# MediCare for All — Pharmacy Management System

A local Spring Boot + SQL Server pharmacy system with a public customer site and seven secure staff workspaces. The professional UI uses the supplied MediCare for All logo, a consistent responsive left sidebar, form validation, motion-safe animations, and role-specific pages.

## Seven roles and their connected CRUD workspaces

| Role | Main CRUD responsibility | Connected handover |
|---|---|---|
| Admin | Staff accounts, access status, full workflow oversight | Can open every module and keep audit history intact |
| Inventory Supervisor | Medicine records: add, view, edit, search, and delete | Makes medicine, pricing, reorder level, and prescription requirement visible to connected roles |
| Cashier | Customer orders, payment, checkout, invoices | Releases branch stock and creates invoices automatically |
| Duty Pharmacist | Uploaded prescription review, decision, audit note, archive | Approved records unlock prescription-only checkout |
| Operations Manager | Branches, promotions, delivery board, workflow overview | Campaign discounts apply automatically at checkout |
| Procurement Coordinator | Suppliers, promotion campaigns, purchase orders, goods received | Goods receipt creates batch/branch stock and audit entries |
| Customer Feedback Manager | Feedback cases, priorities, owners, responses, CSAT | Tracks service improvement from customer feedback to closure |

Delivery is managed by the **Operations Manager** as a connected workflow, instead of being a separate staff login role.

## Clean Inventory Supervisor workspace

The Inventory Supervisor now uses one focused **Medicine Records** workspace. It contains only the required CRUD actions: create a medicine, view/search the list, edit a record, and delete an unused record. Legacy Stock Centre, Transfers, and Prescription Rules URLs safely redirect to Medicine Records so old bookmarks do not open extra screens.

## Connected system flow

`Supplier → Purchase Order → Goods Received → Batch / Branch Stock → Prescription Review (when required) → Checkout → Promotion Discount → Payment → Invoice → Delivery Board → Feedback Case → Admin Oversight`

The six assignment activity diagrams are available in [docs/ACTIVITY_DIAGRAMS.md](docs/ACTIVITY_DIAGRAMS.md).

## SQL Server setup (SSMS)

Run these files in this exact order against `PharmacyManagementDB`:

1. `database/00_base_schema.sql`
2. `database/01_inventory_connected_workflow.sql`
3. `database/02_pharmacist_cashier_stockout.sql`
4. `database/03_procurement_inventory_receipts.sql`
5. `database/04_delivery_operations_admin.sql`
6. `database/05_professional_portal_upgrade.sql`
7. `database/06_promotion_supplier_crud_fix.sql`
8. `database/07_permanent_delete_workflow.sql`
9. Optional: `database/99_verify_data.sql`

The last upgrade script safely converts an old Delivery Management role to Customer Feedback Manager, retains existing records, adds invoice and promotion tables, enhances feedback cases, protects prescription archives, and adds supplier business fields. The promotion/supplier fix makes promotion CRUD available to the Procurement Coordinator and changes supplier removal to a permanent delete while preserving existing purchase-order history. The permanent-delete workflow then enables an intentional permanent delete for both medicine records and promotion campaigns, including their directly related child records.

## Run in IntelliJ

1. Open this `PharmacyManagement` folder in IntelliJ IDEA.
2. Set Project SDK and Maven runner to **JDK 21**.
3. Set your local SQL Server values in `src/main/resources/application.properties`.
4. Reload Maven and run `PharmacyManagementApplication`.
5. Open [http://localhost:8081](http://localhost:8081).

## Presentation demo data

On first application run, safe local demo data is created automatically (only if missing): seven accounts, three branches, twelve medicines with low-stock and expiry examples, a supplier, purchase orders, prescription decisions, a paid order with invoice, a promotion campaign, a delivery, and feedback cases.

| Role | Demo email | Demo password |
|---|---|---|
| Admin | `admin@medicare.local` | `Admin@2026` |
| Inventory Supervisor | `inventory@medicare.local` | `Inventory@2026` |
| Cashier | `cashier@medicare.local` | `Cashier@2026` |
| Duty Pharmacist | `pharmacist@medicare.local` | `Pharmacist@2026` |
| Operations Manager | `operations@medicare.local` | `Operations@2026` |
| Procurement Coordinator | `procurement@medicare.local` | `Procurement@2026` |
| Customer Feedback Manager | `feedback@medicare.local` | `Feedback@2026` |

For a clean database without demo records, set `pharmacy.demo-data.enabled=false` before starting the application.

## Suggested 20-minute demonstration route

1. Sign in as Procurement, show a supplier and purchase order, then receive stock.
2. Sign in as Inventory, add a medicine, search it, edit its prescription requirement, and delete a safe test record.
3. Upload a public prescription, then sign in as Pharmacist and approve/reject it with a note.
4. Sign in as Cashier, complete checkout, show automatic promotion discount and print the invoice.
5. Sign in as Operations, show promotion campaign, branch control, delivery board, and workflow overview.
6. Submit feedback, then sign in as Feedback Manager to assign, update, resolve, and view CSAT.
7. Sign in as Admin to show account controls and full workflow monitoring.
