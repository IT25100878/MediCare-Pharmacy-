# MediCare Pharmacy Management System — Final Release Guide

## 1. What this release contains

This is the final role-based MediCare Pharmacy Management System. It keeps one connected operational flow from supplier receipt to customer feedback while giving every role its own focused workspace and CRUD functions.

## 2. Required software

- Java JDK 21
- IntelliJ IDEA
- SQL Server Management Studio and SQL Server Express
- Database connection configured in `src/main/resources/application.properties`

## 3. First-time database setup

Open the `database` folder in SQL Server Management Studio and run the scripts in this order:

1. `00_schema.sql`
2. `01_seed_data.sql`
3. `02_queries_procedure_trigger.sql`
4. `03_feedback_management.sql`
5. `04_invoice_delivery_workflow.sql`
6. `05_inventory_procurement_workflow.sql`
7. `06_promotion_supplier_crud_fix.sql`
8. `07_permanent_delete_workflow.sql`

`99_inspect_all_tables.sql` is optional. Use it only to view the sample database records.

## 4. Run the website

1. Open this `PharmacyManagement` folder in IntelliJ IDEA.
2. Confirm the project SDK is **JDK 21**.
3. Check the SQL Server credentials and database name in `application.properties`.
4. Run `PharmacyManagementApplication`.
5. Open `http://localhost:8081` in the browser.

## 5. Connected end-to-end workflow

1. **Procurement Coordinator** creates suppliers, purchase orders, goods-received records, and promotion campaigns.
2. **Inventory Supervisor** adds, edits, searches, and permanently deletes medicine records when business rules allow it.
3. **Duty Pharmacist** reviews prescriptions and approves or rejects them with a recorded reason.
4. **Cashier** creates an order, applies valid campaigns, completes payment, reduces stock, and produces an invoice.
5. **Operations Manager** manages branches, campaigns, overview data, and delivery visibility.
6. **Feedback Management** captures and resolves customer feedback cases and CSAT records.
7. **Admin** manages users and observes all role workflows from one secure workspace.

## 6. Final quality checks completed

- All HTML template links and static asset references were checked.
- JavaScript syntax was checked for every file in `src/main/resources/static/js`.
- CSS brace structure was checked for every file in `src/main/resources/static/css`.
- Role navigation was cleaned so each displayed link opens an authorised workspace.
- Duplicate navigation labels and obsolete archive wording were removed.
- Supplier, medicine, and promotion permanent-delete messaging matches the implemented workflow.

## 7. Important usage notes

- Use the role-specific sidebar and page navigation; each role sees only its relevant actions.
- Feedback is available from every role workspace through the shared navigation.
- Keep existing sample data for the final demonstration so the dashboard, tables, invoices, and workflow screens show realistic records.
- Test a full presentation flow in this order: supplier → medicine → prescription → checkout → invoice → delivery → feedback.
