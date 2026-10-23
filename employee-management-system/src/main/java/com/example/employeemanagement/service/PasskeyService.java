package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.PasskeyCredential;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.PasskeyCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Lists / removes the passkeys of the signed-in user (registration itself is handled by Spring Security WebAuthn). */
@Service
@RequiredArgsConstructor
@Transactional
public class PasskeyService {

    private final PasskeyCredentialRepository passkeyRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<PasskeyCredential> list(String username) {
        User u = userService.getByUsername(username);
        return passkeyRepository.findByUserOrderByCreatedDesc(u);
    }

    public void delete(String username, Long id) {
        PasskeyCredential c = passkeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passkey not found"));
        if (!c.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your passkey");
        }
        passkeyRepository.delete(c);
    }
}
