package com.pharmacy.management.controller;

import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.service.CustomerOrderService;
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

import java.math.BigDecimal;

@Controller
@RequestMapping("/cashier")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String keyword, Model model) {
        populateList(model, keyword);
        model.addAttribute("order", new CustomerOrder());
        return "cashier/orders";
    }

    @PostMapping("/orders")
    public String create(@Valid @ModelAttribute("order") CustomerOrder order,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        addDiscountError(order, bindingResult);
        if (bindingResult.hasErrors()) {
            populateList(model, null);
            return "cashier/orders";
        }

        try {
            customerOrderService.create(order, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Customer order was created successfully.");
            return "redirect:/cashier/orders";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("orderNumber", "order.duplicate", "This order number already exists.");
            populateList(model, null);
            return "cashier/orders";
        }
    }

    @GetMapping("/orders/{orderId}/edit")
    public String edit(@PathVariable Integer orderId, Model model) {
        model.addAttribute("order", customerOrderService.findById(orderId));
        return "cashier/order-form";
    }

    @PostMapping("/orders/{orderId}/edit")
    public String update(@PathVariable Integer orderId,
                         @Valid @ModelAttribute("order") CustomerOrder order,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        addDiscountError(order, bindingResult);
        if (bindingResult.hasErrors()) {
            order.setOrderId(orderId);
            return "cashier/order-form";
        }

        try {
            customerOrderService.update(orderId, order);
            redirectAttributes.addFlashAttribute("successMessage", "Customer order was updated successfully.");
            return "redirect:/cashier/orders";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.rejectValue("orderNumber", "order.duplicate", "This order number already exists.");
            order.setOrderId(orderId);
            return "cashier/order-form";
        }
    }

    @PostMapping("/orders/{orderId}/delete")
    public String delete(@PathVariable Integer orderId, RedirectAttributes redirectAttributes) {
        try {
            customerOrderService.delete(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Customer order was cancelled and retained in the audit history.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/cashier/orders";
    }

    private void populateList(Model model, String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        model.addAttribute("orders", customerOrderService.findAll(safeKeyword));
        model.addAttribute("keyword", safeKeyword);
    }

    private void addDiscountError(CustomerOrder order, BindingResult bindingResult) {
        BigDecimal total = order.getTotalAmount();
        BigDecimal discount = order.getDiscountAmount();
        if (total != null && discount != null && discount.compareTo(total) > 0) {
            bindingResult.rejectValue("discountAmount", "order.discount.invalid",
                    "Discount cannot be greater than the total amount.");
        }
    }
}
