package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Entity
@Table(name = "salaries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "salary_month", "salary_year"}))
@Getter
@Setter
@NoArgsConstructor
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "salary_month", nullable = false)
    private int salaryMonth;

    @Column(name = "salary_year", nullable = false)
    private int salaryYear;

    @Column(precision = 12, scale = 2) private BigDecimal basic = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal hra = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal allowances = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal bonus = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal grossSalary = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal pfDeduction = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal taxDeduction = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal otherDeductions = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal totalDeductions = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal netSalary = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SalaryStatus status = SalaryStatus.GENERATED;

    private LocalDateTime generatedAt;

    @Column(length = 300)
    private String remarks;

    public String getPeriodLabel() {
        return Month.of(salaryMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + salaryYear;
    }
}
