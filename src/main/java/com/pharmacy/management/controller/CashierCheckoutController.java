package com.pharmacy.management.controller;

import com.pharmacy.management.dto.CashierCheckoutForm;
import com.pharmacy.management.entity.CustomerOrder;
import com.pharmacy.management.service.CashierCheckoutService;
import com.pharmacy.management.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cashier")
public class CashierCheckoutController {

    private final CashierCheckoutService cashierCheckoutService;
    private final InvoiceService invoiceService;

    public CashierCheckoutController(CashierCheckoutService cashierCheckoutService,
                                     InvoiceService invoiceService) {
        this.cashierCheckoutService = cashierCheckoutService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        populateModel(model);
        model.addAttribute("checkoutForm", new CashierCheckoutForm());
        return "cashier/checkout";
    }

    @PostMapping("/checkout")
    public String completeCheckout(@Valid @ModelAttribute("checkoutForm") CashierCheckoutForm checkoutForm,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "cashier/checkout";
        }

        try {
            CustomerOrder order = cashierCheckoutService.checkout(checkoutForm, authentication.getName());
            Integer invoiceId = invoiceService.findByOrderId(order.getOrderId()).getInvoiceId();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Checkout completed. Stock was released and the invoice is ready to print.");
            return "redirect:/cashier/invoices/" + invoiceId;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("checkout.failed", exception.getMessage());
            populateModel(model);
            return "cashier/checkout";
        }
    }

    private void populateModel(Model model) {
        model.addAttribute("batches", cashierCheckoutService.findSaleableBatches());
        model.addAttribute("branches", cashierCheckoutService.findActiveBranches());
        model.addAttribute("approvedPrescriptions", cashierCheckoutService.findApprovedPrescriptions());
        model.addAttribute("recentOrders", cashierCheckoutService.findRecentCompletedOrders());
    }
}

