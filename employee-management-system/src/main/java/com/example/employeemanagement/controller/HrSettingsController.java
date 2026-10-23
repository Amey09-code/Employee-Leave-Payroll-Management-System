package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.HolidayForm;
import com.example.employeemanagement.dto.PasswordForm;
import com.example.employeemanagement.dto.SettingsForm;
import com.example.employeemanagement.service.HolidayService;
import com.example.employeemanagement.service.PasskeyService;
import com.example.employeemanagement.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hr/settings")
@RequiredArgsConstructor
public class HrSettingsController {

    private final SettingService settingService;
    private final HolidayService holidayService;
    private final PasskeyService passkeyService;

    @GetMapping
    public String settings(Authentication auth, Model model) {
        if (!model.containsAttribute("settingsForm")) {
            model.addAttribute("settingsForm", settingService.load());
        }
        if (!model.containsAttribute("holidayForm")) {
            model.addAttribute("holidayForm", new HolidayForm());
        }
        return view(auth, model);
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("settingsForm") SettingsForm form, BindingResult result,
                       Authentication auth, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("holidayForm", new HolidayForm());
            return view(auth, model);
        }
        settingService.update(form);
        ra.addFlashAttribute("success", "Settings saved.");
        return "redirect:/hr/settings";
    }

    @PostMapping("/holidays")
    public String addHoliday(@Valid @ModelAttribute("holidayForm") HolidayForm form, BindingResult result,
                             Authentication auth, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("settingsForm", settingService.load());
            return view(auth, model);
        }
        holidayService.add(form);
        ra.addFlashAttribute("success", "Holiday added.");
        return "redirect:/hr/settings";
    }

    @PostMapping("/holidays/{id}/delete")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes ra) {
        holidayService.delete(id);
        ra.addFlashAttribute("success", "Holiday removed.");
        return "redirect:/hr/settings";
    }

    private String view(Authentication auth, Model model) {
        model.addAttribute("holidays", holidayService.all());
        model.addAttribute("passkeys", passkeyService.list(auth.getName()));
        model.addAttribute("passwordForm", new PasswordForm());
        return "hr/settings";
    }
}
