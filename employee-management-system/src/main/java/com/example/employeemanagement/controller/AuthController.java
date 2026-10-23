package com.example.employeemanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AuthController {

    public static boolean isAuthenticated(Authentication a) {
        return a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken);
    }

    public static boolean isHr(Authentication a) {
        return a != null && a.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_HR"::equals);
    }

    public static String dashboardFor(Authentication a) {
        return isHr(a) ? "/hr/dashboard" : "/employee/dashboard";
    }

    public static String securityPageFor(Authentication a) {
        return isHr(a) ? "/hr/settings" : "/employee/security";
    }

    @GetMapping("/")
    public String home(Authentication auth) {
        return isAuthenticated(auth) ? "redirect:" + dashboardFor(auth) : "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        return isAuthenticated(auth) ? "redirect:" + dashboardFor(auth) : "auth/login";
    }

    @GetMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied() {
        return "error/403";
    }
}
