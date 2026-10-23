package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.PayrollForm;
import com.example.employeemanagement.dto.PayrollSettings;
import com.example.employeemanagement.dto.PayrollSummary;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.Salary;
import com.example.employeemanagement.entity.SalaryStatus;
import com.example.employeemanagement.exception.BusinessException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.SalaryRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Payroll formulas (monthly):
 *   HRA        = basic x HRA%            (default 20%)
 *   Allowances = basic x allowance%      (default 10%)
 *   Gross      = basic + HRA + allowances + bonus
 *   PF         = basic x PF%             (default 12%)
 *   Tax        = gross x slab            (gross <= 3000: 0%, <= 5000: 5%, above: 10%)
 *   Deductions = PF + tax + other deductions
 *   Net        = gross - deductions
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final SalaryRepository salaryRepository;
    private final EmployeeService employeeService;
    private final SettingService settingService;

    /** Generates payslips; existing payslips for the same employee/period are skipped. Returns the number created. */
    public int generate(PayrollForm f) {
        YearMonth period = YearMonth.of(f.getYear(), f.getMonth());
        if (period.isAfter(YearMonth.now())) {
            throw new BusinessException("Cannot generate payroll for a future month.");
        }
        PayrollSettings settings = settingService.payrollSettings();
        BigDecimal bonus = nz(f.getBonus());
        BigDecimal other = nz(f.getOtherDeductions());

        List<Employee> targets = f.getEmployeeId() == null
                ? employeeService.findActive()
                : List.of(employeeService.get(f.getEmployeeId()));

        int created = 0;
        for (Employee e : targets) {
            if (salaryRepository.findByEmployeeAndSalaryMonthAndSalaryYear(e, f.getMonth(), f.getYear()).isPresent()) {
                continue;
            }
            Salary s = calculate(e, bonus, other, settings);
            s.setSalaryMonth(f.getMonth());
            s.setSalaryYear(f.getYear());
            salaryRepository.save(s);
            created++;
        }
        if (created == 0) {
            throw new BusinessException("Nothing to generate: payslips for this period already exist.");
        }
        return created;
    }

    Salary calculate(Employee e, BigDecimal bonus, BigDecimal other, PayrollSettings s) {
        BigDecimal basic = e.getBasicSalary().setScale(2, RoundingMode.HALF_UP);
        BigDecimal hra = pct(basic, s.hraPercent());
        BigDecimal allowances = pct(basic, s.allowancePercent());
        BigDecimal gross = basic.add(hra).add(allowances).add(bonus);
        BigDecimal pf = pct(basic, s.pfPercent());
        BigDecimal tax = gross.multiply(taxRate(gross)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deductions = pf.add(tax).add(other);
        BigDecimal net = gross.subtract(deductions);
        if (net.signum() < 0) {
            throw new BusinessException("Deductions exceed gross pay for " + e.getFullName() + ".");
        }
        Salary sal = new Salary();
        sal.setEmployee(e);
        sal.setBasic(basic);
        sal.setHra(hra);
        sal.setAllowances(allowances);
        sal.setBonus(bonus);
        sal.setGrossSalary(gross);
        sal.setPfDeduction(pf);
        sal.setTaxDeduction(tax);
        sal.setOtherDeductions(other);
        sal.setTotalDeductions(deductions);
        sal.setNetSalary(net);
        sal.setStatus(SalaryStatus.GENERATED);
        sal.setGeneratedAt(LocalDateTime.now());
        return sal;
    }

    private static BigDecimal taxRate(BigDecimal gross) {
        if (gross.compareTo(BigDecimal.valueOf(3000)) <= 0) return BigDecimal.ZERO;
        if (gross.compareTo(BigDecimal.valueOf(5000)) <= 0) return new BigDecimal("0.05");
        return new BigDecimal("0.10");
    }

    private static BigDecimal pct(BigDecimal base, BigDecimal percent) {
        return base.multiply(percent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }

    public Salary markPaid(Long id) {
        Salary s = find(id);
        s.setStatus(SalaryStatus.PAID);
        return salaryRepository.save(s);
    }

    @Transactional(readOnly = true)
    public Page<Salary> list(Integer month, Integer year, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), size);
        if (month != null && year != null) {
            return salaryRepository.findBySalaryMonthAndSalaryYearOrderByEmployeeFirstNameAsc(month, year, pr);
        }
        return salaryRepository.findAllByOrderBySalaryYearDescSalaryMonthDescIdDesc(pr);
    }

    @Transactional(readOnly = true)
    public List<Salary> historyFor(String username) {
        Employee e = employeeService.getByUsername(username);
        return salaryRepository.findByEmployeeOrderBySalaryYearDescSalaryMonthDesc(e);
    }

    @Transactional(readOnly = true)
    public Salary latestFor(String username) {
        Employee e = employeeService.getByUsername(username);
        return salaryRepository.findFirstByEmployeeOrderBySalaryYearDescSalaryMonthDesc(e).orElse(null);
    }

    @Transactional(readOnly = true)
    public PayrollSummary summary(int month, int year) {
        return new PayrollSummary(YearMonth.of(year, month).toString(),
                salaryRepository.countForPeriod(month, year),
                salaryRepository.sumGross(month, year),
                salaryRepository.sumDeductions(month, year),
                salaryRepository.sumNet(month, year));
    }

    @Transactional(readOnly = true)
    public List<PayrollSummary> trend(int months) {
        List<PayrollSummary> out = new ArrayList<>();
        YearMonth ym = YearMonth.now();
        for (int i = 0; i < months; i++) {
            out.add(summary(ym.getMonthValue(), ym.getYear()));
            ym = ym.minusMonths(1);
        }
        return out;
    }

    /** HR may fetch any payslip; an employee only their own (otherwise AccessDeniedException -> 403). */
    @Transactional(readOnly = true)
    public byte[] payslipPdf(Long id, String username, boolean hr) {
        Salary s = find(id);
        if (!hr && !s.getEmployee().getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your payslip");
        }
        Employee e = s.getEmployee();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font h1 = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(13, 110, 253));
            Font h2 = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);
            Font bold = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL);

            Paragraph title = new Paragraph(settingService.companyName(), h1);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            Paragraph sub = new Paragraph("Payslip - " + s.getPeriodLabel(), h2);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(18);
            doc.add(sub);

            PdfPTable info = new PdfPTable(4);
            info.setWidthPercentage(100);
            info.setSpacingAfter(14);
            info(info, "Employee", e.getFullName(), bold, normal);
            info(info, "Employee ID", e.getEmployeeCode(), bold, normal);
            info(info, "Designation", e.getDesignation() == null ? "-" : e.getDesignation(), bold, normal);
            info(info, "Department", e.getDepartment() == null ? "-" : e.getDepartment().getName(), bold, normal);
            doc.add(info);

            PdfPTable t = new PdfPTable(new float[]{3, 2, 3, 2});
            t.setWidthPercentage(100);
            head(t, "Earnings", bold); head(t, "Amount", bold); head(t, "Deductions", bold); head(t, "Amount", bold);
            row(t, "Basic", s.getBasic(), "Provident Fund", s.getPfDeduction(), normal);
            row(t, "House Rent Allowance", s.getHra(), "Income Tax", s.getTaxDeduction(), normal);
            row(t, "Other Allowances", s.getAllowances(), "Other Deductions", s.getOtherDeductions(), normal);
            row(t, "Bonus", s.getBonus(), "", null, normal);
            row(t, "Gross Earnings", s.getGrossSalary(), "Total Deductions", s.getTotalDeductions(), bold);
            doc.add(t);

            Paragraph net = new Paragraph("Net Pay: " + money(s.getNetSalary()), new Font(Font.HELVETICA, 16, Font.BOLD, new Color(25, 135, 84)));
            net.setSpacingBefore(16);
            net.setAlignment(Element.ALIGN_RIGHT);
            doc.add(net);

            Paragraph foot = new Paragraph("Status: " + s.getStatus() + "   |   Generated on "
                    + (s.getGeneratedAt() == null ? LocalDate.now() : s.getGeneratedAt().toLocalDate())
                    + "   |   This is a system generated payslip.", new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            foot.setSpacingBefore(30);
            doc.add(foot);
            doc.close();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Could not generate payslip", ex);
        }
        return out.toByteArray();
    }

    private static void info(PdfPTable t, String label, String value, Font bold, Font normal) {
        PdfPCell l = new PdfPCell(new Phrase(label, bold));
        PdfPCell v = new PdfPCell(new Phrase(value, normal));
        l.setBorder(0); v.setBorder(0);
        t.addCell(l); t.addCell(v);
    }

    private static void head(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new Color(230, 238, 250));
        c.setPadding(6);
        t.addCell(c);
    }

    private static void row(PdfPTable t, String l1, BigDecimal a1, String l2, BigDecimal a2, Font f) {
        cell(t, l1, f, Element.ALIGN_LEFT);
        cell(t, a1 == null ? "" : money(a1), f, Element.ALIGN_RIGHT);
        cell(t, l2, f, Element.ALIGN_LEFT);
        cell(t, a2 == null ? "" : money(a2), f, Element.ALIGN_RIGHT);
    }

    private static void cell(PdfPTable t, String text, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(align);
        c.setPadding(6);
        t.addCell(c);
    }

    private static String money(BigDecimal v) {
        return String.format("%,.2f", v);
    }

    private Salary find(Long id) {
        return salaryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));
    }
}
