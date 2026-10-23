package com.example.employeemanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

    /** Business rule violations are shown as a flash message on the page the user came from. */
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        String target = "/";
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                String path = URI.create(referer).getPath(); // path only, never an external host
                if (path != null && path.startsWith("/") && !path.startsWith("//")) {
                    target = path;
                }
            } catch (IllegalArgumentException ignored) {
                // fall back to "/"
            }
        }
        return "redirect:" + target;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, org.springframework.ui.Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }
}
