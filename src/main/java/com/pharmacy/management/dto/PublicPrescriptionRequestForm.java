package com.pharmacy.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PublicPrescriptionRequestForm {

    @NotBlank(message = "Please enter the patient's full name.")
    @Size(max = 120, message = "Name cannot be longer than 120 characters.")
    private String customerName;

    @NotBlank(message = "Please enter a contact phone number.")
    @Size(max = 30, message = "Phone number cannot be longer than 30 characters.")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,30}$", message = "Enter a valid phone number.")
    private String customerPhone;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}
