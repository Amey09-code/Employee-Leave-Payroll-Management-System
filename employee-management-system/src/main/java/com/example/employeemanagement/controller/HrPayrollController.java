package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.PayrollForm;
import com.example.employeemanagement.entity.Salary;
import com.example.employeemanagement.service.EmployeeService;
import com.example.employeemanagement.service.PayrollService;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/hr/payroll")
@RequiredArgsConstructor
public class HrPayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        if (!model.containsAttribute("payrollForm")) {
            PayrollForm f = new PayrollForm();
            f.setMonth(LocalDate.now().getMonthValue());
            f.setYear(LocalDate.now().getYear());
            model.addAttribute("payrollForm", f);
        }
        return view(month, year, page, model);
    }

    @PostMapping("/generate")
    public String generate(@Valid @ModelAttribute("payrollForm") PayrollForm form, BindingResult result,
                           Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return view(null, null, 0, model);
        }
        int n = payrollService.generate(form);
        ra.addFlashAttribute("success", n + " payslip(s) generated. Existing ones were skipped.");
        return "redirect:/hr/payroll?month=" + form.getMonth() + "&year=" + form.getYear();
    }

    @PostMapping("/{id}/paid")
    public String markPaid(@PathVariable Long id, RedirectAttributes ra) {
        Salary s = payrollService.markPaid(id);
        ra.addFlashAttribute("success", "Marked as paid: " + s.getEmployee().getFullName() + " (" + s.getPeriodLabel() + ").");
        return "redirect:/hr/payroll?month=" + s.getSalaryMonth() + "&year=" + s.getSalaryYear();
    }

    @GetMapping("/{id}/payslip")
    public ResponseEntity<byte[]> payslip(@PathVariable Long id, Authentication auth) {
        byte[] pdf = payrollService.payslipPdf(id, auth.getName(), true);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDisposition(ContentDisposition.attachment().filename("payslip-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(h).body(pdf);
    }

    private String view(Integer month, Integer year, int page, Model model) {
        Page<Salary> p = payrollService.list(month, year, page, 10);
        model.addAttribute("page", p);
        model.addAttribute("salaries", p.getContent());
        model.addAttribute("filterMonth", month);
        model.addAttribute("filterYear", year);
        model.addAttribute("employees", employeeService.findActive());
        model.addAttribute("pagerUrl", "/hr/payroll");
        model.addAttribute("pagerExtra", WebUtil.qs("month", month == null ? null : month.toString(),
                "year", year == null ? null : year.toString()));
        if (month != null && year != null) {
            model.addAttribute("summary", payrollService.summary(month, year));
        }
        return "hr/payroll";
    }
}
