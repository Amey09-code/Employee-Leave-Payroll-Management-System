package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.HolidayForm;
import com.example.employeemanagement.entity.Holiday;
import com.example.employeemanagement.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayService {

    private final HolidayRepository holidayRepository;

    @Transactional(readOnly = true)
    public List<Holiday> upcoming() {
        return holidayRepository.findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Holiday> all() {
        return holidayRepository.findAllByOrderByHolidayDateAsc();
    }

    public Holiday add(HolidayForm f) {
        Holiday h = new Holiday();
        h.setHolidayDate(f.getHolidayDate());
        h.setName(f.getName().trim());
        return holidayRepository.save(h);
    }

    public void delete(Long id) {
        holidayRepository.deleteById(id);
    }
}
