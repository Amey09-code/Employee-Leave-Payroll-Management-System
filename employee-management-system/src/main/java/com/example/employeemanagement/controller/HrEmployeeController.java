package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeForm;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.Role;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.service.DepartmentService;
import com.example.employeemanagement.service.EmployeeService;
import com.example.employeemanagement.util.WebUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/employees")
@RequiredArgsConstructor
public class HrEmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Employee> p = employeeService.search(q, page, 10);
        model.addAttribute("page", p);
        model.addAttribute("employees", p.getContent());
        model.addAttribute("q", q);
        model.addAttribute("pagerUrl", "/hr/employees");
        model.addAttribute("pagerExtra", WebUtil.qs("q", q));
        return "hr/employees";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        return form(model, false);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("employeeForm") EmployeeForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                Employee e = employeeService.create(form);
                ra.addFlashAttribute("success", "Employee " + e.getFullName() + " created (" + e.getEmployeeCode() + ").");
                return "redirect:/hr/employees";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return form(model, false);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("employeeForm", employeeService.toForm(id));
        return form(model, true);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("employeeForm") EmployeeForm form,
                         BindingResult result, Authentication auth, Model model, RedirectAttributes ra) {
        form.setId(id);
        if (!result.hasErrors()) {
            try {
                employeeService.update(id, form, auth.getName());
                ra.addFlashAttribute("success", "Employee updated.");
                return "redirect:/hr/employees";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return form(model, true);
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Employee e = employeeService.toggleActive(id, auth.getName());
        ra.addFlashAttribute("success", e.getFullName() + (e.isActive() ? " activated." : " deactivated."));
        return "redirect:/hr/employees";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        employeeService.delete(id, auth.getName());
        ra.addFlashAttribute("success", "Employee deleted.");
        return "redirect:/hr/employees";
    }

    private String form(Model model, boolean editing) {
        model.addAttribute("editing", editing);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("roles", Role.values());
        return "hr/employee-form";
    }
}
