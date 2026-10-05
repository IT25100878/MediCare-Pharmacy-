package com.pharmacy.management.dto;

import com.pharmacy.management.entity.Feedback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FeedbackCaseForm {

    @NotBlank(message = "Case status is required.")
    @Pattern(regexp = "NEW|IN_PROGRESS|RESOLVED|CLOSED", message = "Choose a valid case status.")
    private String feedbackStatus;

    @NotBlank(message = "Case category is required.")
    @Pattern(regexp = "GENERAL|SERVICE|ORDER|PRESCRIPTION|INVENTORY|COMPLAINT", message = "Choose a valid category.")
    private String category;

    @NotBlank(message = "Case priority is required.")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "Choose a valid priority.")
    private String priority;

    private Integer assignedToUserId;

    @Size(max = 2000, message = "Response cannot exceed 2000 characters.")
    private String adminResponse;

    public static FeedbackCaseForm from(Feedback feedback) {
        FeedbackCaseForm form = new FeedbackCaseForm();
        form.setFeedbackStatus(feedback.getFeedbackStatus());
        form.setCategory(feedback.getCategory());
        form.setPriority(feedback.getPriority());
        form.setAssignedToUserId(feedback.getAssignedToUserId());
        form.setAdminResponse(feedback.getAdminResponse());
        return form;
    }

    public String getFeedbackStatus() { return feedbackStatus; }
    public void setFeedbackStatus(String feedbackStatus) { this.feedbackStatus = feedbackStatus; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Integer getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Integer assignedToUserId) { this.assignedToUserId = assignedToUserId; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
}

