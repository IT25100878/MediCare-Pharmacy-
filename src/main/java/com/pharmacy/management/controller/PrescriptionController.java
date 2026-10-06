package com.pharmacy.management.controller;


import com.pharmacy.management.entity.Prescription;
import com.pharmacy.management.service.PrescriptionFileStorageService;
import com.pharmacy.management.service.PrescriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pharmacist")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionFileStorageService fileStorageService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  PrescriptionFileStorageService fileStorageService) {
        this.prescriptionService = prescriptionService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/prescriptions")
    public String prescriptions(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                Model model) {
        populateList(model, keyword, status);
        return "pharmacist/prescriptions";
    }

    @GetMapping("/prescriptions/new")
    public String newPrescriptionForm() {
        return "pharmacist/prescription-upload-form";
    }

    @PostMapping("/prescriptions/new")
    public String createPrescription(
            @RequestParam String prescriptionNumber,
            @RequestParam String customerName,
            @RequestParam String customerPhone,
            @RequestParam(value = "prescriptionFile", required = false) MultipartFile prescriptionFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Prescription prescription = new Prescription();
            prescription.setPrescriptionNumber(prescriptionNumber);
            prescription.setCustomerName(customerName);
            prescription.setCustomerPhone(customerPhone);
            prescription.setPrescriptionStatus("PENDING");

            if (prescriptionFile != null && !prescriptionFile.isEmpty()) {
                String filePath = fileStorageService.store(prescriptionFile);
                prescription.setPrescriptionFilePath(filePath);
            }

            prescriptionService.create(prescription, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Prescription record was created and placed in the PENDING queue.");
            return "redirect:/pharmacist/prescriptions";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/pharmacist/prescriptions/new";
        }
    }

    @GetMapping("/prescriptions/{prescriptionId}/edit")
    public String edit(@PathVariable Integer prescriptionId, Model model) {
        model.addAttribute("prescription", prescriptionService.findById(prescriptionId));
        return "pharmacist/prescription-form";
    }

    @PostMapping("/prescriptions/{prescriptionId}/edit")
    public String update(@PathVariable Integer prescriptionId,
                         @RequestParam String prescriptionStatus,
                         @RequestParam(required = false) String pharmacistNote,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            prescriptionService.review(prescriptionId, prescriptionStatus, pharmacistNote, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Prescription decision and pharmacist audit note were saved.");
            return "redirect:/pharmacist/prescriptions";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("prescription", prescriptionService.findById(prescriptionId));
            model.addAttribute("reviewError", exception.getMessage());
            return "pharmacist/prescription-form";
        }
    }

    @PostMapping("/prescriptions/{prescriptionId}/delete")
    public String delete(@PathVariable Integer prescriptionId, RedirectAttributes redirectAttributes) {
        prescriptionService.delete(prescriptionId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Prescription was archived. Its file and clinical audit trail remain protected.");
        return "redirect:/pharmacist/prescriptions";
    }

    private void populateList(Model model, String keyword, String status) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String safeStatus = status == null ? "" : status.trim().toUpperCase();
        model.addAttribute("prescriptions", prescriptionService.findAll(safeKeyword, safeStatus));
        model.addAttribute("keyword", safeKeyword);
        model.addAttribute("selectedStatus", safeStatus);
    }
}
