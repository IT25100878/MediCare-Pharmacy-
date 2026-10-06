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
