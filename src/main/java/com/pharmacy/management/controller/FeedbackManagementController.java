package com.pharmacy.management.controller;

import com.pharmacy.management.dto.FeedbackCaseForm;
import com.pharmacy.management.entity.Feedback;
import com.pharmacy.management.service.AdminUserService;
import com.pharmacy.management.service.FeedbackManagementService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback-management")
public class FeedbackManagementController {

    private final FeedbackManagementService feedbackManagementService;
    private final AdminUserService adminUserService;

    public FeedbackManagementController(FeedbackManagementService feedbackManagementService,
                                        AdminUserService adminUserService) {
        this.feedbackManagementService = feedbackManagementService;
        this.adminUserService = adminUserService;
    }

    @GetMapping({"/dashboard", "/analytics"})
    public String analytics(Model model) {
        model.addAttribute("analytics", feedbackManagementService.analytics());
        model.addAttribute("recentCases", feedbackManagementService.findCases("", "", false).stream().limit(8).toList());
        return "feedback-management/analytics";
    }

    @GetMapping("/cases")
    public String cases(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "false") boolean includeArchived,
                        Model model) {
        model.addAttribute("cases", feedbackManagementService.findCases(keyword, status, includeArchived));
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("selectedStatus", status == null ? "" : status.trim().toUpperCase());
        model.addAttribute("includeArchived", includeArchived);
        return "feedback-management/cases";
    }

    @GetMapping("/cases/{feedbackId}/edit")
    public String edit(@PathVariable Integer feedbackId, Model model) {
        Feedback feedback = feedbackManagementService.findById(feedbackId);
        model.addAttribute("feedback", feedback);
        model.addAttribute("caseForm", FeedbackCaseForm.from(feedback));
        model.addAttribute("users", adminUserService.findAllUsers());
        return "feedback-management/case-form";
    }

    @PostMapping("/cases/{feedbackId}/edit")
    public String update(@PathVariable Integer feedbackId,
                         @Valid @ModelAttribute("caseForm") FeedbackCaseForm caseForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Feedback feedback = feedbackManagementService.findById(feedbackId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("feedback", feedback);
            model.addAttribute("users", adminUserService.findAllUsers());
            return "feedback-management/case-form";
        }
        try {
            feedbackManagementService.update(feedbackId, caseForm);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback case was updated and its owner/status were saved.");
            return "redirect:/feedback-management/cases";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("feedback.update.failed", exception.getMessage());
            model.addAttribute("feedback", feedback);
            model.addAttribute("users", adminUserService.findAllUsers());
            return "feedback-management/case-form";
        }
    }

    @PostMapping("/cases/{feedbackId}/archive")
    public String archive(@PathVariable Integer feedbackId, RedirectAttributes redirectAttributes) {
        feedbackManagementService.archive(feedbackId);
        redirectAttributes.addFlashAttribute("successMessage", "Feedback case was archived; its audit record remains available to Admin.");
        return "redirect:/feedback-management/cases";
    }
}
