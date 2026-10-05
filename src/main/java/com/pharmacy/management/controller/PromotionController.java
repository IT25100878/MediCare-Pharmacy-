package com.pharmacy.management.controller;

import com.pharmacy.management.entity.Promotion;
import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.service.PharmacyBranchService;
import com.pharmacy.management.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
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

@Controller
@RequestMapping({"/operations/promotions", "/procurement/promotions"})
public class PromotionController {

    private final PromotionService promotionService;
    private final MedicineService medicineService;
    private final PharmacyBranchService pharmacyBranchService;

    public PromotionController(PromotionService promotionService,
                               MedicineService medicineService,
                               PharmacyBranchService pharmacyBranchService) {
        this.promotionService = promotionService;
        this.medicineService = medicineService;
        this.pharmacyBranchService = pharmacyBranchService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       HttpServletRequest request,
                       Model model) {
        String promotionBasePath = resolvePromotionBasePath(request);
        populateList(model, keyword, promotionBasePath);
        model.addAttribute("promotion", new Promotion());
        return "operations/promotions";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("promotion") Promotion promotion,
                         BindingResult bindingResult,
                         Authentication authentication,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String promotionBasePath = resolvePromotionBasePath(request);
        if (bindingResult.hasErrors()) {
            populateList(model, null, promotionBasePath);
            return "operations/promotions";
        }
        try {
            promotionService.create(promotion, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Promotion campaign was created and will apply automatically during checkout.");
            return "redirect:" + promotionBasePath;
        } catch (IllegalArgumentException | DataIntegrityViolationException exception) {
            bindingResult.reject("promotion.create.failed", exception.getMessage());
            populateList(model, null, promotionBasePath);
            return "operations/promotions";
        }
    }

    @GetMapping("/{promotionId}/edit")
    public String edit(@PathVariable Integer promotionId,
                       HttpServletRequest request,
                       Model model) {
        model.addAttribute("promotion", promotionService.findById(promotionId));
        populateOptions(model);
        populateWorkspace(model, resolvePromotionBasePath(request));
        return "operations/promotion-form";
    }

    @PostMapping("/{promotionId}/edit")
    public String update(@PathVariable Integer promotionId,
                         @Valid @ModelAttribute("promotion") Promotion promotion,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String promotionBasePath = resolvePromotionBasePath(request);
        if (bindingResult.hasErrors()) {
            promotion.setPromotionId(promotionId);
            populateOptions(model);
            populateWorkspace(model, promotionBasePath);
            return "operations/promotion-form";
        }
        try {
            promotionService.update(promotionId, promotion);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion campaign was updated.");
            return "redirect:" + promotionBasePath;
        } catch (IllegalArgumentException | DataIntegrityViolationException exception) {
            bindingResult.reject("promotion.update.failed", exception.getMessage());
            promotion.setPromotionId(promotionId);
            populateOptions(model);
            populateWorkspace(model, promotionBasePath);
            return "operations/promotion-form";
        }
    }

    @PostMapping("/{promotionId}/delete")
    public String delete(@PathVariable Integer promotionId,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            promotionService.delete(promotionId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Promotion campaign and its redemption records were permanently deleted.");
        } catch (DataAccessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The permanent delete database procedure is not ready. Run database/07_permanent_delete_workflow.sql in SSMS, then try again.");
        }
        return "redirect:" + resolvePromotionBasePath(request);
    }

    private void populateList(Model model, String keyword, String promotionBasePath) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        model.addAttribute("promotions", promotionService.findAll(safeKeyword));
        model.addAttribute("keyword", safeKeyword);
        populateOptions(model);
        populateWorkspace(model, promotionBasePath);
    }

    private void populateOptions(Model model) {
        model.addAttribute("medicines", medicineService.findAll(""));
        model.addAttribute("branches", pharmacyBranchService.findAll(""));
    }

    private void populateWorkspace(Model model, String promotionBasePath) {
        boolean procurementWorkspace = promotionBasePath.startsWith("/procurement/");
        model.addAttribute("promotionBasePath", promotionBasePath);
        model.addAttribute("promotionWorkspace", procurementWorkspace ? "PROCUREMENT" : "OPERATIONS");
        model.addAttribute("promotionRoleLabel", procurementWorkspace
                ? "Procurement Coordinator"
                : "Operations Manager");
    }

    private String resolvePromotionBasePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)
                ? requestUri.substring(contextPath.length())
                : requestUri;
        return path.startsWith("/procurement/")
                ? "/procurement/promotions"
                : "/operations/promotions";
    }
}

