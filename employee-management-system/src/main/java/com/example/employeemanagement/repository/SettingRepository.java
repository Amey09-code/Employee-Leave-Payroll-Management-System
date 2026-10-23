package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, String> {
}
