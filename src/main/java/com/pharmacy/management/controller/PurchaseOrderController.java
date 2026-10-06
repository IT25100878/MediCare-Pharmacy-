package com.pharmacy.management.controller;

import com.pharmacy.management.entity.PurchaseOrder;
import com.pharmacy.management.service.PurchaseOrderService;
import jakarta.validation.Valid;
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

import java.time.LocalDate;


@Controller
@RequestMapping("/procurement")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }


    @GetMapping("/purchase-orders")
    public String purchaseOrders(@RequestParam(required = false) String keyword, Model model) {
        populateList(model, keyword);
        model.addAttribute("purchaseOrder", new PurchaseOrder());
        return "procurement/purchase-orders";
    }

    @PostMapping("/purchase-orders")
    public String create(@Valid @ModelAttribute("purchaseOrder") PurchaseOrder purchaseOrder,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        addExpectedDateError(purchaseOrder, bindingResult);
        if (bindingResult.hasErrors()) {
            populateList(model, null);
            return "procurement/purchase-orders";
        }

        try {
            purchaseOrderService.create(purchaseOrder, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Purchase order was created successfully.");
            return "redirect:/procurement/purchase-orders";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("purchaseOrderNumber", "purchaseOrder.duplicate",
                    "This purchase order number already exists.");
            populateList(model, null);
            return "procurement/purchase-orders";
        }
    }

    @GetMapping("/purchase-orders/{purchaseOrderId}/edit")
    public String edit(@PathVariable Integer purchaseOrderId, Model model) {
        model.addAttribute("purchaseOrder", purchaseOrderService.findById(purchaseOrderId));
        model.addAttribute("suppliers", purchaseOrderService.findAllSuppliers());
        return "procurement/purchase-order-form";
    }

    @PostMapping("/purchase-orders/{purchaseOrderId}/edit")
    public String update(@PathVariable Integer purchaseOrderId,
                         @Valid @ModelAttribute("purchaseOrder") PurchaseOrder purchaseOrder,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        addExpectedDateError(purchaseOrder, bindingResult);
        if (bindingResult.hasErrors()) {
            purchaseOrder.setPurchaseOrderId(purchaseOrderId);
            model.addAttribute("suppliers", purchaseOrderService.findAllSuppliers());
            return "procurement/purchase-order-form";
        }

        try {
            purchaseOrderService.update(purchaseOrderId, purchaseOrder);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase order was updated successfully.");
            return "redirect:/procurement/purchase-orders";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("purchaseOrderNumber", "purchaseOrder.duplicate",
                    "This purchase order number already exists.");
            purchaseOrder.setPurchaseOrderId(purchaseOrderId);
            model.addAttribute("suppliers", purchaseOrderService.findAllSuppliers());
            return "procurement/purchase-order-form";
        }
    }

    @PostMapping("/purchase-orders/{purchaseOrderId}/delete")
    public String delete(@PathVariable Integer purchaseOrderId, RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.delete(purchaseOrderId);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase order was cancelled and retained in procurement history.");
            return "redirect:/procurement/purchase-orders";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/procurement/purchase-orders";
        }
    }

    private void populateList(Model model, String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        model.addAttribute("purchaseOrders", purchaseOrderService.findAll(safeKeyword));
        model.addAttribute("suppliers", purchaseOrderService.findAllSuppliers());
        model.addAttribute("keyword", safeKeyword);
    }

    private void addExpectedDateError(PurchaseOrder purchaseOrder, BindingResult bindingResult) {
        LocalDate orderDate = purchaseOrder.getOrderDate();
        LocalDate expectedDate = purchaseOrder.getExpectedDate();
        if (orderDate != null && expectedDate != null && expectedDate.isBefore(orderDate)) {
            bindingResult.rejectValue("expectedDate", "purchaseOrder.date.invalid",
                    "Expected date cannot be before the order date.");
        }
    }
}
