package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.service.AttendanceService;
import com.example.employeemanagement.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class HrAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        LocalDate d = date == null ? LocalDate.now() : date;
        Page<Attendance> p = attendanceService.byDate(d, page, 15);
        model.addAttribute("page", p);
        model.addAttribute("records", p.getContent());
        model.addAttribute("date", d);
        model.addAttribute("pagerUrl", "/hr/attendance");
        model.addAttribute("pagerExtra", WebUtil.qs("date", d.toString()));
        model.addAttribute("presentToday", attendanceService.presentToday());
        model.addAttribute("absentToday", attendanceService.absentToday());
        return "hr/attendance";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] csv = attendanceService.exportCsv(from, to).getBytes(StandardCharsets.UTF_8);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        h.setContentDisposition(ContentDisposition.attachment().filename("attendance_" + from + "_" + to + ".csv").build());
        return ResponseEntity.ok().headers(h).body(csv);
    }
}
