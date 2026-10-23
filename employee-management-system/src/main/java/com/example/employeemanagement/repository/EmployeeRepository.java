package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserUsername(String username);
    List<Employee> findByActiveTrueOrderByFirstNameAsc();
    long countByActiveTrue();
    long countByDepartmentId(Long departmentId);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("select max(e.id) from Employee e")
    Long findMaxId();

    @Query("""
           select e from Employee e left join e.department d
           where lower(e.firstName) like lower(concat('%', :q, '%'))
              or lower(e.lastName) like lower(concat('%', :q, '%'))
              or lower(e.employeeCode) like lower(concat('%', :q, '%'))
              or lower(e.email) like lower(concat('%', :q, '%'))
              or lower(coalesce(e.designation, '')) like lower(concat('%', :q, '%'))
              or lower(coalesce(d.name, '')) like lower(concat('%', :q, '%'))
           """)
    Page<Employee> search(@Param("q") String q, Pageable pageable);
}
