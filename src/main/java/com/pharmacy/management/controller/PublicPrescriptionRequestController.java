package com.pharmacy.management.controller;

import com.pharmacy.management.dto.PublicPrescriptionRequestForm;
import com.pharmacy.management.service.PublicPrescriptionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicPrescriptionRequestController {

    private final PublicPrescriptionService publicPrescriptionService;

    public PublicPrescriptionRequestController(PublicPrescriptionService publicPrescriptionService) {
        this.publicPrescriptionService = publicPrescriptionService;
    }

    @GetMapping("/prescription-request")
    public String requestForm(Model model) {
        if (!model.containsAttribute("prescriptionRequest")) {
            model.addAttribute("prescriptionRequest", new PublicPrescriptionRequestForm());
        }
        return "storefront/prescription-request";
    }

    @PostMapping("/prescription-request")
    public String submit(@Valid @ModelAttribute("prescriptionRequest") PublicPrescriptionRequestForm form,
                         BindingResult bindingResult,
                         @RequestParam("prescriptionFile") MultipartFile prescriptionFile,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "storefront/prescription-request";
        }

        try {
            String requestNumber = publicPrescriptionService.submit(form, prescriptionFile);
            redirectAttributes.addFlashAttribute("requestNumber", requestNumber);
            return "redirect:/prescription-request?submitted";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("prescriptionFile", exception.getMessage());
            return "storefront/prescription-request";
        }
    }
}

