package com.example.employeemanagement.config;

import com.example.employeemanagement.controller.AuthController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** After password login, HR goes to /hr/dashboard and employees to /employee/dashboard. */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) ->
                response.sendRedirect(request.getContextPath() + AuthController.dashboardFor(authentication));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler successHandler,
                                                   @Value("${app.webauthn.rp-id}") String rpId,
                                                   @Value("${app.webauthn.rp-name}") String rpName,
                                                   @Value("${app.webauthn.allowed-origins}") String allowedOrigins) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/access-denied", "/css/**", "/js/**", "/images/**", "/error", "/error/**").permitAll()
                // Passkey sign-in endpoints must be reachable before authentication
                .requestMatchers("/webauthn/authenticate/**", "/login/webauthn").permitAll()
                // Strict server-side role separation
                .requestMatchers("/hr/**").hasRole("HR")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll())
            // Passkeys / biometric sign-in (Spring Security native WebAuthn). Credentials are stored via
            // JpaUserCredentialRepository in the passkey_credentials table.
            .webAuthn(webAuthn -> webAuthn
                .rpName(rpName)
                .rpId(rpId)
                .allowedOrigins(allowedOrigins.split(",")))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .sessionManagement(session -> session.sessionFixation(fix -> fix.migrateSession()))
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));
        return http.build();
    }
}
