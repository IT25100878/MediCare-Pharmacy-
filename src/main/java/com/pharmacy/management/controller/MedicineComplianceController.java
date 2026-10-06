package com.pharmacy.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Keeps old bookmarks working after the inventory workspace was simplified.
 * Prescription requirement is now edited on the medicine record itself.
 */
@Controller
@RequestMapping("/inventory")
public class MedicineComplianceController {

    @GetMapping("/medicine-requirements")
    public String medicineRequirements() {
        return "redirect:/inventory/medicines";
    }
}
