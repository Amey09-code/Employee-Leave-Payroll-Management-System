package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.DepartmentForm;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/departments")
@RequiredArgsConstructor
public class HrDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        DepartmentForm form = new DepartmentForm();
        if (edit != null) {
            Department d = departmentService.get(edit);
            form.setId(d.getId());
            form.setName(d.getName());
            form.setDescription(d.getDescription());
        }
        model.addAttribute("departmentForm", form);
        return view(model);
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("departmentForm") DepartmentForm form, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return view(model);
        }
        boolean isNew = form.getId() == null;
        departmentService.save(form);
        ra.addFlashAttribute("success", isNew ? "Department created." : "Department updated.");
        return "redirect:/hr/departments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        departmentService.delete(id);
        ra.addFlashAttribute("success", "Department deleted.");
        return "redirect:/hr/departments";
    }

    private String view(Model model) {
        model.addAttribute("rows", departmentService.findAllWithCounts());
        return "hr/departments";
    }
}
