package com.pharmacy.management.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "Suppliers", schema = "dbo")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SupplierId")
    private Integer supplierId;

    @NotBlank(message = "Supplier name is required.")
    @Size(max = 150, message = "Supplier name cannot exceed 150 characters.")
    @Column(name = "SupplierName", nullable = false, length = 150)
    private String supplierName;

    @Size(max = 120, message = "Contact person cannot exceed 120 characters.")
    @Column(name = "ContactPerson", length = 120)
    private String contactPerson;

    @NotBlank(message = "Contact number is required.")
    @Size(max = 30, message = "Contact number cannot exceed 30 characters.")
    @Column(name = "ContactNumber", nullable = false, length = 30)
    private String contactNumber;

    @Email(message = "Enter a valid email address.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    @Column(name = "Email", length = 150)
    private String email;

    @Size(max = 300, message = "Address cannot exceed 300 characters.")
    @Column(name = "Address", length = 300)
    private String address;
