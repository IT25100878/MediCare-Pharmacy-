package com.pharmacy.management.controller;

import com.pharmacy.management.dto.FeedbackForm;
import com.pharmacy.management.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/feedback")
    public String feedbackForm(Model model) {
        if (!model.containsAttribute("feedbackForm")) {
            model.addAttribute("feedbackForm", new FeedbackForm());
        }
        return "feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@Valid @ModelAttribute("feedbackForm") FeedbackForm feedbackForm,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "feedback";
        }

        String currentUserEmail = authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? null : authentication.getName();
        feedbackService.submit(feedbackForm, currentUserEmail);
        redirectAttributes.addFlashAttribute("feedbackSuccess",
                "Thank you. Your feedback case has been saved for the Customer Feedback Manager.");
        return "redirect:/feedback";
    }
}
