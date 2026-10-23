package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.DepartmentForm;
import com.example.employeemanagement.dto.DepartmentRow;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<DepartmentRow> findAllWithCounts() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .map(d -> new DepartmentRow(d, employeeRepository.countByDepartmentId(d.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Department get(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    public Department save(DepartmentForm form) {
        String name = form.getName().trim();
        Department d;
        if (form.getId() == null) {
            if (departmentRepository.existsByNameIgnoreCase(name)) {
                throw new BusinessException("A department with this name already exists.");
            }
            d = new Department();
        } else {
            d = get(form.getId());
            if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, d.getId())) {
                throw new BusinessException("A department with this name already exists.");
            }
        }
        d.setName(name);
        d.setDescription(form.getDescription());
        return departmentRepository.save(d);
    }

    public void delete(Long id) {
        Department d = get(id);
        if (employeeRepository.countByDepartmentId(id) > 0) {
            throw new BusinessException("Department still has employees. Move them first.");
        }
        departmentRepository.delete(d);
    }
}
