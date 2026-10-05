package com.pharmacy.management.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FeedbackForm {

    @NotBlank(message = "Your name is required.")
    @Size(max = 120, message = "Name cannot be longer than 120 characters.")
    private String customerName;

    @Size(max = 30, message = "Phone number cannot be longer than 30 characters.")
    private String customerPhone;

    @NotBlank(message = "Subject is required.")
    @Size(max = 150, message = "Subject cannot be longer than 150 characters.")
    private String subject;

    @NotBlank(message = "Feedback message is required.")
    @Size(max = 2000, message = "Feedback message cannot be longer than 2000 characters.")
    private String message;

    @NotNull(message = "Please choose a rating.")
    @Min(value = 1, message = "Rating must be between 1 and 5.")
    @Max(value = 5, message = "Rating must be between 1 and 5.")
    private Integer rating = 5;

    @NotBlank(message = "Please choose a feedback category.")
    @Pattern(regexp = "GENERAL|SERVICE|ORDER|PRESCRIPTION|INVENTORY|COMPLAINT", message = "Choose a valid category.")
    private String category = "GENERAL";

    @NotBlank(message = "Please choose a priority.")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "Choose a valid priority.")
    private String priority = "MEDIUM";

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

