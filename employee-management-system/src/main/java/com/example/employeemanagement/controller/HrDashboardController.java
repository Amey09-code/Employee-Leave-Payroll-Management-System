package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.LeaveStatus;
import com.example.employeemanagement.service.DashboardService;
import com.example.employeemanagement.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HrDashboardController {

    private final DashboardService dashboardService;
    private final LeaveService leaveService;

    @GetMapping("/hr/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.hrStats());
        model.addAttribute("pendingLeaves", leaveService.list(LeaveStatus.PENDING, 0, 5).getContent());
        return "hr/dashboard";
    }
}
