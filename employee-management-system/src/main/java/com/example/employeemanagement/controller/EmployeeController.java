package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.LeaveForm;
import com.example.employeemanagement.dto.PasswordForm;
import com.example.employeemanagement.dto.ProfileForm;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.LeaveRequest;
import com.example.employeemanagement.entity.LeaveType;
import com.example.employeemanagement.service.*;
import com.example.employeemanagement.util.WebUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Everything here is resolved from the authenticated principal (auth.getName()): there is no employee id in any URL,
 * so an employee can never reach another employee's profile, attendance, leave or salary data.
 */
@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final DashboardService dashboardService;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final HolidayService holidayService;
    private final PasskeyService passkeyService;

    // ---- dashboard ----
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        model.addAttribute("overview", dashboardService.employeeOverview(auth.getName()));
        return "employee/dashboard";
    }

    // ---- profile ----
    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        Employee e = employeeService.getByUsername(auth.getName());
        if (!model.containsAttribute("profileForm")) {
            ProfileForm f = new ProfileForm();
            f.setPhone(e.getPhone());
            f.setAddress(e.getAddress());
            model.addAttribute("profileForm", f);
        }
        model.addAttribute("employee", e);
        return "employee/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm form, BindingResult result,
                                Authentication auth, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("employee", employeeService.getByUsername(auth.getName()));
            return "employee/profile";
        }
        employeeService.updateProfile(auth.getName(), form);
        ra.addFlashAttribute("success", "Profile updated.");
        return "redirect:/employee/profile";
    }

    // ---- attendance ----
    @GetMapping("/attendance")
    public String attendance(@RequestParam(defaultValue = "0") int page, Authentication auth, Model model) {
        Page<?> p = attendanceService.history(auth.getName(), page, 10);
        model.addAttribute("page", p);
        model.addAttribute("records", p.getContent());
        model.addAttribute("today", attendanceService.today(auth.getName()).orElse(null));
        model.addAttribute("pagerUrl", "/employee/attendance");
        model.addAttribute("pagerExtra", "");
        return "employee/attendance";
    }

    @PostMapping("/attendance/check-in")
    public String checkIn(Authentication auth, RedirectAttributes ra) {
        attendanceService.checkIn(auth.getName());
        ra.addFlashAttribute("success", "Checked in. Have a productive day!");
        return "redirect:/employee/attendance";
    }

    @PostMapping("/attendance/check-out")
    public String checkOut(Authentication auth, RedirectAttributes ra) {
        attendanceService.checkOut(auth.getName());
        ra.addFlashAttribute("success", "Checked out. See you tomorrow!");
        return "redirect:/employee/attendance";
    }

    // ---- leave ----
    @GetMapping("/leaves")
    public String leaves(@RequestParam(defaultValue = "0") int page, Authentication auth, Model model) {
        if (!model.containsAttribute("leaveForm")) {
            model.addAttribute("leaveForm", new LeaveForm());
        }
        return leaveView(page, auth, model);
    }

    @PostMapping("/leaves")
    public String applyLeave(@Valid @ModelAttribute("leaveForm") LeaveForm form, BindingResult result,
                             Authentication auth, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return leaveView(0, auth, model);
        }
        leaveService.apply(auth.getName(), form);
        ra.addFlashAttribute("success", "Leave request submitted for approval.");
        return "redirect:/employee/leaves";
    }

    @PostMapping("/leaves/{id}/cancel")
    public String cancelLeave(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        leaveService.cancel(auth.getName(), id);
        ra.addFlashAttribute("success", "Leave request cancelled.");
        return "redirect:/employee/leaves";
    }

    private String leaveView(int page, Authentication auth, Model model) {
        Page<LeaveRequest> p = leaveService.myLeaves(auth.getName(), page, 8);
        model.addAttribute("page", p);
        model.addAttribute("leaves", p.getContent());
        model.addAttribute("balance", leaveService.balanceFor(auth.getName()));
        model.addAttribute("leaveTypes", LeaveType.values());
        model.addAttribute("pagerUrl", "/employee/leaves");
        model.addAttribute("pagerExtra", WebUtil.qs());
        return "employee/leaves";
    }

    // ---- salary ----
    @GetMapping("/salary")
    public String salary(Authentication auth, Model model) {
        model.addAttribute("salaries", payrollService.historyFor(auth.getName()));
        return "employee/salary";
    }

    @GetMapping("/salary/{id}/payslip")
    public ResponseEntity<byte[]> payslip(@PathVariable Long id, Authentication auth) {
        byte[] pdf = payrollService.payslipPdf(id, auth.getName(), false); // ownership enforced in the service
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDisposition(ContentDisposition.attachment().filename("payslip-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(h).body(pdf);
    }

    // ---- holidays ----
    @GetMapping("/holidays")
    public String holidays(Model model) {
        model.addAttribute("holidays", holidayService.upcoming());
        return "employee/holidays";
    }

    // ---- password & biometric ----
    @GetMapping("/security")
    public String security(Authentication auth, Model model) {
        model.addAttribute("passkeys", passkeyService.list(auth.getName()));
        model.addAttribute("passwordForm", new PasswordForm());
        return "employee/security";
    }
}
