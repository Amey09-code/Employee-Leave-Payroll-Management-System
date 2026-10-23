package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.LeaveRequest;
import com.example.employeemanagement.entity.LeaveStatus;
import com.example.employeemanagement.entity.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);
    Page<LeaveRequest> findByEmployee(Employee employee, Pageable pageable);
    long countByStatus(LeaveStatus status);
    boolean existsByEmployee(Employee employee);

    @Query("""
           select coalesce(sum(l.days), 0) from LeaveRequest l
           where l.employee = :e and l.status = :status and l.leaveType <> :excluded
             and l.startDate between :from and :to
           """)
    long sumDays(@Param("e") Employee e, @Param("status") LeaveStatus status, @Param("excluded") LeaveType excluded,
                 @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
           select count(l) from LeaveRequest l
           where l.employee = :e and l.status in :statuses
             and l.startDate <= :end and l.endDate >= :start
           """)
    long countOverlapping(@Param("e") Employee e, @Param("statuses") Collection<LeaveStatus> statuses,
                          @Param("start") LocalDate start, @Param("end") LocalDate end);
}
