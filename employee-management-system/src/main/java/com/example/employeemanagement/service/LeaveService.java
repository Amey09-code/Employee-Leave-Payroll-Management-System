package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.LeaveBalance;
import com.example.employeemanagement.dto.LeaveForm;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;
    private final SettingService settingService;

    public LeaveRequest apply(String username, LeaveForm f) {
        Employee e = employeeService.getByUsername(username);
        if (f.getEndDate().isBefore(f.getStartDate())) {
            throw new BusinessException("End date cannot be before the start date.");
        }
        if (f.getStartDate().isBefore(LocalDate.now().minusDays(7))) {
            throw new BusinessException("Leave cannot start more than 7 days in the past.");
        }
        int days = workingDays(f.getStartDate(), f.getEndDate());
        if (days == 0) {
            throw new BusinessException("The selected range contains no working days.");
        }
        if (leaveRequestRepository.countOverlapping(e, List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                f.getStartDate(), f.getEndDate()) > 0) {
            throw new BusinessException("You already have a pending or approved leave overlapping these dates.");
        }
        if (f.getLeaveType() != LeaveType.UNPAID && days > balance(e).remaining()) {
            throw new BusinessException("Not enough leave balance. Remaining: " + balance(e).remaining() + " day(s).");
        }
        LeaveRequest l = new LeaveRequest();
        l.setEmployee(e);
        l.setLeaveType(f.getLeaveType());
        l.setStartDate(f.getStartDate());
        l.setEndDate(f.getEndDate());
        l.setDays(days);
        l.setReason(f.getReason().trim());
        l.setStatus(LeaveStatus.PENDING);
        return leaveRequestRepository.save(l);
    }

    /** Employees can withdraw their own pending requests only. */
    public void cancel(String username, Long leaveId) {
        LeaveRequest l = find(leaveId);
        if (!l.getEmployee().getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your leave request");
        }
        if (l.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Only pending requests can be cancelled.");
        }
        leaveRequestRepository.delete(l);
    }

    public LeaveRequest decide(Long leaveId, boolean approve, String comment) {
        LeaveRequest l = find(leaveId);
        if (l.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("This request has already been decided.");
        }
        if (approve && l.getLeaveType() != LeaveType.UNPAID && l.getDays() > balance(l.getEmployee()).remaining()) {
            throw new BusinessException("Employee does not have enough leave balance to approve this request.");
        }
        l.setStatus(approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        l.setHrComment(comment == null || comment.isBlank() ? null : comment.trim());
        l.setDecidedAt(LocalDateTime.now());
        return leaveRequestRepository.save(l);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequest> myLeaves(String username, int page, int size) {
        Employee e = employeeService.getByUsername(username);
        return leaveRequestRepository.findByEmployee(e,
                PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "appliedAt")));
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequest> list(LeaveStatus status, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        return status == null ? leaveRequestRepository.findAll(pr) : leaveRequestRepository.findByStatus(status, pr);
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        return leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countByStatus(LeaveStatus status) {
        return leaveRequestRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public LeaveBalance balance(Employee e) {
        int year = LocalDate.now().getYear();
        int allowance = settingService.annualLeaveAllowance();
        int used = (int) leaveRequestRepository.sumDays(e, LeaveStatus.APPROVED, LeaveType.UNPAID,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        return new LeaveBalance(allowance, used, Math.max(0, allowance - used));
    }

    @Transactional(readOnly = true)
    public LeaveBalance balanceFor(String username) {
        return balance(employeeService.getByUsername(username));
    }

    static int workingDays(LocalDate from, LocalDate to) {
        int n = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            DayOfWeek w = d.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) {
                n++;
            }
        }
        return n;
    }

    private LeaveRequest find(Long id) {
        return leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
    }
}
