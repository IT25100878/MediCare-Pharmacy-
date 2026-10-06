package com.pharmacy.management.controller;

import com.pharmacy.management.service.WorkflowAnalyticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkflowOverviewController {

    private final WorkflowAnalyticsService workflowAnalyticsService;

    public WorkflowOverviewController(WorkflowAnalyticsService workflowAnalyticsService) {
        this.workflowAnalyticsService = workflowAnalyticsService;
    }

    @GetMapping("/operations/overview")
    public String operationsOverview(Model model) {
        populate(model, "OPERATIONS");
        return "workflow/overview";
    }

    @GetMapping("/admin/workflow-monitor")
    public String adminWorkflowMonitor(Model model) {
        populate(model, "ADMIN");
        return "workflow/overview";
    }

    private void populate(Model model, String viewMode) {
        model.addAttribute("viewMode", viewMode);
        model.addAttribute("analytics", workflowAnalyticsService.getSnapshot());
        model.addAttribute("recentOrders", workflowAnalyticsService.findRecentPaidOrders());
        model.addAttribute("recentDeliveries", workflowAnalyticsService.findRecentDeliveries());
    }
}

