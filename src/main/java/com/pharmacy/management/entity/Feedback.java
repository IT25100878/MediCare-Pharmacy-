package com.pharmacy.management.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;


@Entity
@Table(name = "Feedback", schema = "dbo")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeedbackId")
    private Integer feedbackId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "CustomerName", length = 120)
    private String customerName;

    @Column(name = "CustomerPhone", length = 30)
    private String customerPhone;

    @Column(name = "Subject", nullable = false, length = 150)
    private String subject;

    @Column(name = "Message", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "FeedbackStatus", nullable = false, length = 20)
    private String feedbackStatus = "NEW";

    @Column(name = "Category", nullable = false, length = 40)
    private String category = "GENERAL";

    @Column(name = "Priority", nullable = false, length = 20)
    private String priority = "MEDIUM";

    @Column(name = "AssignedToUserId")
    private Integer assignedToUserId;

    @Column(name = "IsArchived", nullable = false)
    private boolean archived = false;

    @Column(name = "AdminResponse", columnDefinition = "NVARCHAR(MAX)")
    private String adminResponse;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "ClosedAt")
    private LocalDateTime closedAt;
