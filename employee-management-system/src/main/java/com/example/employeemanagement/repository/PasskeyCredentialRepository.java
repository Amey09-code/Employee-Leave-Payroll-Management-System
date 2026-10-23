package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.PasskeyCredential;
import com.example.employeemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {
    Optional<PasskeyCredential> findByCredentialId(String credentialId);
    List<PasskeyCredential> findByUserOrderByCreatedDesc(User user);
    List<PasskeyCredential> findByUserId(Long userId);
}
