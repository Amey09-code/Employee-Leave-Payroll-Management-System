package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Simple key/value application setting (company name, leave allowance, payroll percentages). */
@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
public class Setting {

    @Id
    @Column(name = "setting_key", length = 100)
    private String key;

    @Column(name = "setting_value", length = 500)
    private String value;

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
