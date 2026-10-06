package com.pharmacy.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class RegistrationForm {

    @NotBlank(message = "Full name is required.")
    @Size(max = 120, message = "Full name cannot be longer than 120 characters.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 150, message = "Email cannot be longer than 150 characters.")
    private String email;

    @NotBlank(message = "Please choose your role.")
    private String roleCode;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must contain 8 to 72 characters.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    private String adminRegistrationCode;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
