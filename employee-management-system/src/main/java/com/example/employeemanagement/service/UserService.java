package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.PasswordForm;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void changePassword(String username, PasswordForm form) {
        User u = getByUsername(username);
        if (!passwordEncoder.matches(form.getCurrentPassword(), u.getPassword())) {
            throw new BusinessException("Current password is incorrect.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("New password and confirmation do not match.");
        }
        if (passwordEncoder.matches(form.getNewPassword(), u.getPassword())) {
            throw new BusinessException("New password must be different from the current one.");
        }
        u.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(u);
    }
}
