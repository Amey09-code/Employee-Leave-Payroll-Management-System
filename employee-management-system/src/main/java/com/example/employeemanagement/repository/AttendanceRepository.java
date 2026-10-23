package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
    Page<Attendance> findByWorkDate(LocalDate workDate, Pageable pageable);
    Page<Attendance> findByEmployeeOrderByWorkDateDesc(Employee employee, Pageable pageable);
    long countByWorkDateAndCheckInIsNotNull(LocalDate workDate);
    boolean existsByEmployee(Employee employee);

    @Query("select a from Attendance a where a.workDate between :from and :to order by a.workDate asc, a.employee.firstName asc")
    List<Attendance> findInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select count(a) from Attendance a where a.employee = :e and a.workDate between :from and :to and a.status <> com.example.employeemanagement.entity.AttendanceStatus.ABSENT")
    long countWorkedDays(@Param("e") Employee e, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
