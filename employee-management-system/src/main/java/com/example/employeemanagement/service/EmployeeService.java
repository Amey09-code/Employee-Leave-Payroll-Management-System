package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeForm;
import com.example.employeemanagement.dto.ProfileForm;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SalaryRepository salaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<Employee> search(String keyword, int page, int size) {
        String q = keyword == null ? "" : keyword.trim();
        return employeeRepository.search(q, PageRequest.of(Math.max(page, 0), size, Sort.by("firstName", "lastName")));
    }

    @Transactional(readOnly = true)
    public List<Employee> findActive() {
        return employeeRepository.findByActiveTrueOrderByFirstNameAsc();
    }

    @Transactional(readOnly = true)
    public Employee get(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Transactional(readOnly = true)
    public Employee getByUsername(String username) {
        return employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No employee record is linked to this account"));
    }

    @Transactional(readOnly = true)
    public EmployeeForm toForm(Long id) {
        Employee e = get(id);
        EmployeeForm f = new EmployeeForm();
        f.setId(e.getId());
        f.setFirstName(e.getFirstName());
        f.setLastName(e.getLastName());
        f.setEmail(e.getEmail());
        f.setPhone(e.getPhone());
        f.setAddress(e.getAddress());
        f.setDateOfBirth(e.getDateOfBirth());
        f.setJoinDate(e.getJoinDate());
        f.setDesignation(e.getDesignation());
        f.setDepartmentId(e.getDepartment() == null ? null : e.getDepartment().getId());
        f.setBasicSalary(e.getBasicSalary());
        f.setUsername(e.getUser().getUsername());
        f.setRole(e.getUser().getRole());
        return f;
    }

    public Employee create(EmployeeForm f) {
        if (f.getPassword() == null || !f.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,100}$")) {
            throw new BusinessException("Initial password must be at least 8 characters with a letter and a digit.");
        }
        if (userRepository.existsByUsername(f.getUsername())) {
            throw new BusinessException("Username is already taken.");
        }
        if (employeeRepository.existsByEmailIgnoreCase(f.getEmail())) {
            throw new BusinessException("An employee with this email already exists.");
        }
        User user = new User();
        user.setUsername(f.getUsername().trim());
        user.setPassword(passwordEncoder.encode(f.getPassword()));
        user.setRole(f.getRole());
        user.setEnabled(true);

        Employee e = new Employee();
        e.setEmployeeCode(nextCode());
        e.setUser(user);
        e.setActive(true);
        apply(e, f);
        return employeeRepository.save(e); // cascades to the user
    }

    public Employee update(Long id, EmployeeForm f, String currentUsername) {
        Employee e = get(id);
        if (e.getUser().getUsername().equals(currentUsername) && e.getUser().getRole() != f.getRole()) {
            throw new BusinessException("You cannot change your own role.");
        }
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(f.getEmail(), id)) {
            throw new BusinessException("Another employee already uses this email.");
        }
        apply(e, f);
        User u = e.getUser();
        u.setRole(f.getRole());
        if (f.getPassword() != null && !f.getPassword().isBlank()) {
            if (!f.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).{8,100}$")) {
                throw new BusinessException("Password must be at least 8 characters with a letter and a digit.");
            }
            u.setPassword(passwordEncoder.encode(f.getPassword()));
        }
        return employeeRepository.save(e);
    }

    /** Activates / deactivates the employee and their login. HR cannot deactivate their own account. */
    public Employee toggleActive(Long id, String currentUsername) {
        Employee e = get(id);
        if (e.getUser().getUsername().equals(currentUsername)) {
            throw new BusinessException("You cannot deactivate your own account.");
        }
        boolean active = !e.isActive();
        e.setActive(active);
        e.getUser().setEnabled(active);
        return employeeRepository.save(e);
    }

    public void delete(Long id, String currentUsername) {
        Employee e = get(id);
        if (e.getUser().getUsername().equals(currentUsername)) {
            throw new BusinessException("You cannot delete your own account.");
        }
        if (attendanceRepository.existsByEmployee(e) || leaveRequestRepository.existsByEmployee(e)
                || salaryRepository.existsByEmployee(e)) {
            throw new BusinessException("This employee has attendance, leave or payroll history. Deactivate instead of deleting.");
        }
        employeeRepository.delete(e); // cascades to the user and passkeys
    }

    public Employee updateProfile(String username, ProfileForm f) {
        Employee e = getByUsername(username);
        e.setPhone(f.getPhone());
        e.setAddress(f.getAddress());
        return employeeRepository.save(e);
    }

    private void apply(Employee e, EmployeeForm f) {
        e.setFirstName(f.getFirstName().trim());
        e.setLastName(f.getLastName().trim());
        e.setEmail(f.getEmail().trim().toLowerCase());
        e.setPhone(f.getPhone());
        e.setAddress(f.getAddress());
        e.setDateOfBirth(f.getDateOfBirth());
        e.setJoinDate(f.getJoinDate());
        e.setDesignation(f.getDesignation());
        e.setBasicSalary(f.getBasicSalary());
        e.setDepartment(f.getDepartmentId() == null ? null
                : departmentRepository.findById(f.getDepartmentId())
                        .orElseThrow(() -> new BusinessException("Selected department does not exist.")));
    }

    private String nextCode() {
        Long max = employeeRepository.findMaxId();
        return String.format("EMP%04d", (max == null ? 0 : max) + 1);
    }
}
