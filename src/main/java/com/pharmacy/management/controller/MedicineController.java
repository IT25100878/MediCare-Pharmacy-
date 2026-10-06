package com.pharmacy.management.controller;

import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
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

import java.math.BigDecimal;

@Controller
@RequestMapping("/inventory")
public class MedicineController {

    private final MedicineService medicineService;
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/medicines")
    public String medicines(@RequestParam(required = false) String keyword, Model model) {
        populateMedicineList(model, keyword);
        model.addAttribute("medicine", new Medicine());
        return "inventory/medicines";
    }

    @PostMapping("/medicines")
    public String createMedicine(@Valid @ModelAttribute("medicine") Medicine medicine,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        addSellingPriceError(medicine, bindingResult);
        if (bindingResult.hasErrors()) {
            populateMedicineList(model, null);
            return "inventory/medicines";
        }

        try {
            medicineService.create(medicine, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Medicine record was created successfully.");
            return "redirect:/inventory/medicines";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("batchNumber", "medicine.duplicate",
                    "This medicine name and batch number already exist.");
            populateMedicineList(model, null);
            return "inventory/medicines";
        }
    }

    @GetMapping("/medicines/{medicineId}/edit")
    public String editMedicine(@PathVariable Integer medicineId, Model model) {
        model.addAttribute("medicine", medicineService.findById(medicineId));
        return "inventory/medicine-form";
    }

    @PostMapping("/medicines/{medicineId}/edit")
    public String updateMedicine(@PathVariable Integer medicineId,
                                 @Valid @ModelAttribute("medicine") Medicine medicine,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        addSellingPriceError(medicine, bindingResult);
        if (bindingResult.hasErrors()) {
            medicine.setMedicineId(medicineId);
            return "inventory/medicine-form";
        }

        try {
            medicineService.update(medicineId, medicine);
            redirectAttributes.addFlashAttribute("successMessage", "Medicine record was updated successfully.");
            return "redirect:/inventory/medicines";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("medicineName", "medicine.duplicate", "This medicine name already exists.");
            medicine.setMedicineId(medicineId);
            return "inventory/medicine-form";
        }
    }

    @PostMapping("/medicines/{medicineId}/delete")
    public String deleteMedicine(@PathVariable Integer medicineId,
                                 RedirectAttributes redirectAttributes) {
        try {
            medicineService.delete(medicineId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Medicine record and its connected stock, receipt, order-item, transfer, and campaign records were permanently deleted.");
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The permanent delete database procedure is not ready. Run database/07_permanent_delete_workflow.sql in SSMS, then try again.");
        }
        return "redirect:/inventory/medicines";
    }

    private void populateMedicineList(Model model, String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        model.addAttribute("medicines", medicineService.findAll(safeKeyword));
        model.addAttribute("keyword", safeKeyword);
        model.addAttribute("lowStockCount", medicineService.countLowStock());
        model.addAttribute("expiringSoonCount", medicineService.countExpiringSoon());
    }

    private void addSellingPriceError(Medicine medicine, BindingResult bindingResult) {
        BigDecimal purchasePrice = medicine.getPurchasePrice();
        BigDecimal sellingPrice = medicine.getSellingPrice();
        if (purchasePrice != null && sellingPrice != null && sellingPrice.compareTo(purchasePrice) < 0) {
            bindingResult.rejectValue("sellingPrice", "medicine.price.invalid",
                    "Selling price must be equal to or greater than purchase price.");
        }
    }
}
