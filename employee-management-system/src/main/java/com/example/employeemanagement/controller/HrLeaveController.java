package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.LeaveRequest;
import com.example.employeemanagement.entity.LeaveStatus;
import com.example.employeemanagement.service.LeaveService;
import com.example.employeemanagement.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/leaves")
@RequiredArgsConstructor
public class HrLeaveController {

    private final LeaveService leaveService;

    @GetMapping
    public String list(@RequestParam(required = false) LeaveStatus status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        Page<LeaveRequest> p = leaveService.list(status, page, 10);
        model.addAttribute("page", p);
        model.addAttribute("leaves", p.getContent());
        model.addAttribute("status", status);
        model.addAttribute("statuses", LeaveStatus.values());
        model.addAttribute("pagerUrl", "/hr/leaves");
        model.addAttribute("pagerExtra", WebUtil.qs("status", status == null ? null : status.name()));
        return "hr/leaves";
    }

    @PostMapping("/{id}/decision")
    public String decide(@PathVariable Long id, @RequestParam String decision,
                         @RequestParam(required = false) String comment, RedirectAttributes ra) {
        boolean approve = "approve".equalsIgnoreCase(decision);
        leaveService.decide(id, approve, comment);
        ra.addFlashAttribute("success", approve ? "Leave approved." : "Leave rejected.");
        return "redirect:/hr/leaves?status=PENDING";
    }
}
