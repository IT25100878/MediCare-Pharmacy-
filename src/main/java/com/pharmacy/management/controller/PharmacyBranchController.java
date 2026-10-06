package com.pharmacy.management.controller;

import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.service.PharmacyBranchService;
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
@RequestMapping("/operations")
public class PharmacyBranchController {

    private final PharmacyBranchService pharmacyBranchService;

    public PharmacyBranchController(PharmacyBranchService pharmacyBranchService) {
        this.pharmacyBranchService = pharmacyBranchService;
    }

    @GetMapping("/branches")
    public String branches(@RequestParam(required = false) String keyword, Model model) {
        populateList(model, keyword);
        model.addAttribute("branch", new PharmacyBranch());
        return "operations/branches";
    }

    @PostMapping("/branches")
    public String create(@Valid @ModelAttribute("branch") PharmacyBranch branch,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateList(model, null);
            return "operations/branches";
        }

        try {
            pharmacyBranchService.create(branch);
            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy branch was added successfully.");
            return "redirect:/operations/branches";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("branchName", "branch.duplicate", "This branch name already exists.");
            populateList(model, null);
            return "operations/branches";
        }
    }

    @GetMapping("/branches/{branchId}/edit")
    public String edit(@PathVariable Integer branchId, Model model) {
        model.addAttribute("branch", pharmacyBranchService.findById(branchId));
        return "operations/branch-form";
    }

    @PostMapping("/branches/{branchId}/edit")
    public String update(@PathVariable Integer branchId,
                         @Valid @ModelAttribute("branch") PharmacyBranch branch,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            branch.setBranchId(branchId);
            return "operations/branch-form";
        }

        try {
            pharmacyBranchService.update(branchId, branch);
            redirectAttributes.addFlashAttribute("successMessage", "Pharmacy branch was updated successfully.");
            return "redirect:/operations/branches";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("branchName", "branch.duplicate", "This branch name already exists.");
            branch.setBranchId(branchId);
            return "operations/branch-form";
        }
    }

    @PostMapping("/branches/{branchId}/delete")
    public String delete(@PathVariable Integer branchId, RedirectAttributes redirectAttributes) {
        pharmacyBranchService.delete(branchId);
        redirectAttributes.addFlashAttribute("successMessage", "Pharmacy branch was deactivated and its stock history remains available.");
        return "redirect:/operations/branches";
    }

    private void populateList(Model model, String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        model.addAttribute("branches", pharmacyBranchService.findAll(safeKeyword));
        model.addAttribute("keyword", safeKeyword);
    }
}
