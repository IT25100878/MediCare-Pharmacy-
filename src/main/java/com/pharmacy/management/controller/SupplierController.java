package com.pharmacy.management.controller;

import com.pharmacy.management.entity.Supplier;
import com.pharmacy.management.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/procurement")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/suppliers")
    public String suppliers(@RequestParam(required = false) String keyword, Model model) {
        populateList(model, keyword);
        model.addAttribute("supplier", new Supplier());
        return "procurement/suppliers";
    }

    @PostMapping("/suppliers")
    public String create(@Valid @ModelAttribute("supplier") Supplier supplier,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateList(model, null);
            return "procurement/suppliers";
        }

        try {
            supplierService.create(supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier was added successfully.");
            return "redirect:/procurement/suppliers";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("supplierName", "supplier.duplicate", "This supplier name already exists.");
            populateList(model, null);
            return "procurement/suppliers";
        }
    }

    @GetMapping("/suppliers/{supplierId}/edit")
    public String edit(@PathVariable Integer supplierId, Model model) {
        model.addAttribute("supplier", supplierService.findById(supplierId));
        return "procurement/supplier-form";
    }

    @PostMapping("/suppliers/{supplierId}/edit")
    public String update(@PathVariable Integer supplierId,
                         @Valid @ModelAttribute("supplier") Supplier supplier,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            supplier.setSupplierId(supplierId);
            return "procurement/supplier-form";
        }

        try {
            supplierService.update(supplierId, supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier was updated successfully.");
            return "redirect:/procurement/suppliers";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("supplierName", "supplier.duplicate", "This supplier name already exists.");
            supplier.setSupplierId(supplierId);
            return "procurement/supplier-form";
        }
    }

    @PostMapping("/suppliers/{supplierId}/delete")
    public String delete(@PathVariable Integer supplierId, RedirectAttributes redirectAttributes) {
        try {
            supplierService.delete(supplierId);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier was permanently deleted.");
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This supplier is linked to a purchase order. Run database/06_promotion_supplier_crud_fix.sql in SSMS, then try again.");
        }
        return "redirect:/procurement/suppliers";
    }

    private void populateList(Model model, String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        model.addAttribute("suppliers", supplierService.findAll(safeKeyword));
        model.addAttribute("keyword", safeKeyword);
    }
}
