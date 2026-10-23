package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.Salary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    Optional<Salary> findByEmployeeAndSalaryMonthAndSalaryYear(Employee employee, int month, int year);
    boolean existsByEmployee(Employee employee);

    Page<Salary> findAllByOrderBySalaryYearDescSalaryMonthDescIdDesc(Pageable pageable);
    Page<Salary> findBySalaryMonthAndSalaryYearOrderByEmployeeFirstNameAsc(int month, int year, Pageable pageable);
    List<Salary> findByEmployeeOrderBySalaryYearDescSalaryMonthDesc(Employee employee);
    Optional<Salary> findFirstByEmployeeOrderBySalaryYearDescSalaryMonthDesc(Employee employee);

    @Query("select count(s) from Salary s where s.salaryMonth = :m and s.salaryYear = :y")
    long countForPeriod(@Param("m") int month, @Param("y") int year);

    @Query("select coalesce(sum(s.grossSalary), 0) from Salary s where s.salaryMonth = :m and s.salaryYear = :y")
    BigDecimal sumGross(@Param("m") int month, @Param("y") int year);

    @Query("select coalesce(sum(s.totalDeductions), 0) from Salary s where s.salaryMonth = :m and s.salaryYear = :y")
    BigDecimal sumDeductions(@Param("m") int month, @Param("y") int year);

    @Query("select coalesce(sum(s.netSalary), 0) from Salary s where s.salaryMonth = :m and s.salaryYear = :y")
    BigDecimal sumNet(@Param("m") int month, @Param("y") int year);
}
