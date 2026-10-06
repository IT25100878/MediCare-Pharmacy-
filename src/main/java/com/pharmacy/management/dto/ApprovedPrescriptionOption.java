package com.pharmacy.management.dto;

public class ApprovedPrescriptionOption {

    private final Integer prescriptionId;
    private final String prescriptionNumber;
    private final String customerName;
    private final String customerPhone;

    public ApprovedPrescriptionOption(Integer prescriptionId,
                                      String prescriptionNumber,
                                      String customerName,
                                      String customerPhone) {
        this.prescriptionId = prescriptionId;
        this.prescriptionNumber = prescriptionNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    public Integer getPrescriptionId() { return prescriptionId; }
    public String getPrescriptionNumber() { return prescriptionNumber; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
}
