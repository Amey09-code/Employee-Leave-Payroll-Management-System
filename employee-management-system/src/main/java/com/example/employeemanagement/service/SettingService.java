package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.PayrollSettings;
import com.example.employeemanagement.dto.SettingsForm;
import com.example.employeemanagement.entity.Setting;
import com.example.employeemanagement.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingService {

    public static final String COMPANY_NAME = "company.name";
    public static final String LEAVE_ALLOWANCE = "leave.annual.allowance";
    public static final String HRA_PERCENT = "payroll.hra.percent";
    public static final String ALLOWANCE_PERCENT = "payroll.allowance.percent";
    public static final String PF_PERCENT = "payroll.pf.percent";

    private final SettingRepository settingRepository;

    private String get(String key, String defaultValue) {
        return settingRepository.findById(key).map(Setting::getValue).orElse(defaultValue);
    }

    private void put(String key, String value) {
        settingRepository.save(new Setting(key, value));
    }

    @Transactional(readOnly = true)
    public String companyName() {
        return get(COMPANY_NAME, "Acme Corp");
    }

    @Transactional(readOnly = true)
    public int annualLeaveAllowance() {
        try {
            return Integer.parseInt(get(LEAVE_ALLOWANCE, "24"));
        } catch (NumberFormatException e) {
            return 24;
        }
    }

    @Transactional(readOnly = true)
    public PayrollSettings payrollSettings() {
        return new PayrollSettings(
                new BigDecimal(get(HRA_PERCENT, "20")),
                new BigDecimal(get(ALLOWANCE_PERCENT, "10")),
                new BigDecimal(get(PF_PERCENT, "12")));
    }

    @Transactional(readOnly = true)
    public SettingsForm load() {
        PayrollSettings p = payrollSettings();
        SettingsForm f = new SettingsForm();
        f.setCompanyName(companyName());
        f.setAnnualLeaveAllowance(annualLeaveAllowance());
        f.setHraPercent(p.hraPercent());
        f.setAllowancePercent(p.allowancePercent());
        f.setPfPercent(p.pfPercent());
        return f;
    }

    public void update(SettingsForm f) {
        put(COMPANY_NAME, f.getCompanyName().trim());
        put(LEAVE_ALLOWANCE, String.valueOf(f.getAnnualLeaveAllowance()));
        put(HRA_PERCENT, f.getHraPercent().toPlainString());
        put(ALLOWANCE_PERCENT, f.getAllowancePercent().toPlainString());
        put(PF_PERCENT, f.getPfPercent().toPlainString());
    }
}
