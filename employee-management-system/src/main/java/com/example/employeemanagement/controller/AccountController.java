package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.PasswordForm;
import com.example.employeemanagement.service.PasskeyService;
import com.example.employeemanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Actions available to every signed-in user (HR and employees): change password, remove a passkey. */
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final PasskeyService passkeyService;

    @PostMapping("/account/password")
    public String changePassword(@Valid @ModelAttribute PasswordForm form, BindingResult result,
                                 Authentication auth, RedirectAttributes ra) {
        String back = "redirect:" + AuthController.securityPageFor(auth);
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", result.getFieldErrors().get(0).getDefaultMessage());
            return back;
        }
        userService.changePassword(auth.getName(), form);
        ra.addFlashAttribute("success", "Password changed successfully.");
        return back;
    }

    @PostMapping("/account/passkeys/{id}/delete")
    public String deletePasskey(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        passkeyService.delete(auth.getName(), id);
        ra.addFlashAttribute("success", "Passkey removed.");
        return "redirect:" + AuthController.securityPageFor(auth);
    }
}
