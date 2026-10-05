# MediCare for All - Activity Diagrams

These activity diagrams match the six team workspaces in the implemented system. Admin is the cross-module controller and can open every workflow.

## 1. Inventory Supervisor - Medicine and stock control

```mermaid
flowchart TD
    A[Open inventory workspace] --> B[Add or update medicine record]
    B --> C{Valid details?}
    C -- No --> D[Show validation message]
    D --> B
    C -- Yes --> E[Save medicine master]
    E --> F[Receive batch at branch]
    F --> G[Update branch stock and audit transaction]
    G --> H{Low stock or expiry risk?}
    H -- Yes --> I[Show inventory alert]
    H -- No --> J[Stock ready for checkout]
    I --> J
```

## 2. Cashier - Checkout and invoice

```mermaid
flowchart TD
    A[Start customer checkout] --> B[Select branch and medicine batch]
    B --> C{Stock available and unexpired?}
    C -- No --> D[Show stock warning]
    D --> B
    C -- Yes --> E{Prescription required?}
    E -- Yes --> F[Select approved prescription]
    F --> G{Customer details match?}
    G -- No --> H[Stop checkout and explain]
    G -- Yes --> I[Apply best active promotion]
    E -- No --> I
    I --> J[Choose cash card or online payment]
    J --> K[Save paid order]
    K --> L[Reduce branch stock and write audit]
    L --> M[Create invoice]
```

## 3. Duty Pharmacist - Prescription review

```mermaid
flowchart TD
    A[Open pending prescription] --> B[Open uploaded JPG PNG or PDF]
    B --> C[Check patient medicine dosage and refill history]
    C --> D{Complete and clinically valid?}
    D -- Yes --> E[Approve with pharmacist note]
    E --> F[Write review time and pharmacist audit]
    F --> G[Approved record becomes selectable at checkout]
    D -- No --> H[Reject and enter clear reason]
    H --> I[Save rejection audit]
```

## 4. Operations Manager - Campaign and delivery coordination

```mermaid
flowchart TD
    A[Open operations workspace] --> B[Create or update campaign]
    B --> C{Dates and discount valid?}
    C -- No --> D[Show validation message]
    D --> B
    C -- Yes --> E[Activate campaign]
    E --> F[Cashier checkout applies matching discount]
    F --> G[Open paid order delivery board]
    G --> H[Assign delivery person]
    H --> I[Track assigned out-for-delivery or delivered status]
    I --> J[Keep delivery event audit]
```

## 5. Procurement Coordinator - Supplier to goods receipt

```mermaid
flowchart TD
    A[Register or update supplier] --> B[Validate business registration and terms]
    B --> C{Supplier record valid?}
    C -- No --> D[Show validation message]
    D --> A
    C -- Yes --> E[Create purchase order]
    E --> F[Supplier delivers goods]
    F --> G[Receive batch against purchase order]
    G --> H[Create or update medicine batch]
    H --> I[Increase selected branch stock]
    I --> J[Write receipt and stock audit]
```

## 6. Customer Feedback Manager - Case management and CSAT

```mermaid
flowchart TD
    A[Customer submits feedback] --> B[Create NEW case]
    B --> C[Feedback Manager opens case]
    C --> D[Set category priority and owner]
    D --> E[Move case to IN PROGRESS]
    E --> F[Write service response]
    F --> G{Issue resolved?}
    G -- No --> E
    G -- Yes --> H[Set RESOLVED or CLOSED]
    H --> I[CSAT dashboard updates]
    I --> J[Archive while retaining audit history]
```

## Admin control

Admin can manage staff access, deactivate accounts without removing audit data, open every role workspace, and monitor the connected workflow dashboard.
