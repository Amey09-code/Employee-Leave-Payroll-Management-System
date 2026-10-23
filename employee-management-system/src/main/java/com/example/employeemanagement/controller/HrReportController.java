package com.example.employeemanagement.controller;

import com.example.employeemanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HrReportController {

    private final ReportService reportService;

    @GetMapping("/hr/reports")
    public String reports(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year, Model model) {
        LocalDate now = LocalDate.now();
        int m = month == null || month < 1 || month > 12 ? now.getMonthValue() : month;
        int y = year == null || year < 2000 || year > 2100 ? now.getYear() : year;
        model.addAttribute("report", reportService.build(m, y));
        return "hr/reports";
    }
}
