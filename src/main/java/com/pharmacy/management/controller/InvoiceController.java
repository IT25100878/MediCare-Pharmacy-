package com.pharmacy.management.controller;

import com.pharmacy.management.dto.InvoiceDetails;
import com.pharmacy.management.entity.Invoice;
import com.pharmacy.management.service.InvoiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cashier/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("invoices", invoiceService.findAll());
        return "cashier/invoices";
    }

    @GetMapping("/{invoiceId}")
    public String preview(@PathVariable Integer invoiceId,
                          @RequestParam(defaultValue = "false") boolean print,
                          Model model) {
        InvoiceDetails details = invoiceService.getDetails(invoiceId);
        model.addAttribute("details", details);
        model.addAttribute("autoPrint", print);
        return "cashier/invoice-preview";
    }

    @PostMapping("/{invoiceId}/print")
    public String print(@PathVariable Integer invoiceId, RedirectAttributes redirectAttributes) {
        invoiceService.markPrinted(invoiceId);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice print record was updated.");
        return "redirect:/cashier/invoices/" + invoiceId + "?print=true";
    }

    @PostMapping("/{invoiceId}/void")
    public String voidInvoice(@PathVariable Integer invoiceId,
                              @RequestParam(required = false) String note,
                              RedirectAttributes redirectAttributes) {
        invoiceService.voidInvoice(invoiceId, note);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice was voided. The original document remains in the audit trail.");
        return "redirect:/cashier/invoices/" + invoiceId;
    }
}
