package com.pharmacy.management.controller;


import com.pharmacy.management.dto.PurchaseOrderReceiptForm;
import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.service.ProcurementReceiptService;
import com.pharmacy.management.service.InventoryWorkflowService;
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
@RequestMapping("/procurement")
public class ProcurementReceiptController {

    private final ProcurementReceiptService procurementReceiptService;
    private final MedicineService medicineService;
    private final InventoryWorkflowService inventoryWorkflowService;

    public ProcurementReceiptController(ProcurementReceiptService procurementReceiptService,
                                        MedicineService medicineService,
                                        InventoryWorkflowService inventoryWorkflowService) {
        this.procurementReceiptService = procurementReceiptService;
        this.medicineService = medicineService;
        this.inventoryWorkflowService = inventoryWorkflowService;
    }

    @GetMapping("/receipts")
    public String receipts(Model model) {
        populateModel(model);
        model.addAttribute("receiptForm", new PurchaseOrderReceiptForm());
        return "procurement/receipts";
    }

    @PostMapping("/receipts")
    public String receive(@Valid @ModelAttribute("receiptForm") PurchaseOrderReceiptForm receiptForm,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "procurement/receipts";
        }

        try {
            procurementReceiptService.receivePurchaseOrder(receiptForm, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Purchase order received. Batch stock, branch stock, medicine summary, and audit history were updated.");
            return "redirect:/procurement/receipts";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("receipt.receive.failed", exception.getMessage());
            populateModel(model);
            return "procurement/receipts";
        }
    }

    private void populateModel(Model model) {
        model.addAttribute("purchaseOrders", procurementReceiptService.findReceivablePurchaseOrders());
        model.addAttribute("medicines", medicineService.findAll(""));
        model.addAttribute("branches", inventoryWorkflowService.getActiveBranches());
        model.addAttribute("receipts", procurementReceiptService.findAllReceipts());
    }
}
