package com.example.employeemanagement.config;

import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maps the WebAuthn "user entity" onto our users table: the opaque user handle is stored in
 * users.passkey_user_handle (base64url) the first time a user registers a passkey.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class JpaPublicKeyCredentialUserEntityRepository implements PublicKeyCredentialUserEntityRepository {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PublicKeyCredentialUserEntity findById(Bytes id) {
        return userRepository.findByPasskeyUserHandle(id.toBase64UrlString()).map(this::toEntity).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicKeyCredentialUserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPasskeyUserHandle() != null)
                .map(this::toEntity)
                .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity entity) {
        if (entity == null) {
            return;
        }
        userRepository.findByUsername(entity.getName()).ifPresent(u -> {
            if (u.getPasskeyUserHandle() == null) {
                u.setPasskeyUserHandle(entity.getId().toBase64UrlString());
                userRepository.save(u);
            }
        });
    }

    @Override
    public void delete(Bytes id) {
        // The handle is kept for the lifetime of the account; credentials are removed via PasskeyService.
    }

    private PublicKeyCredentialUserEntity toEntity(User u) {
        return ImmutablePublicKeyCredentialUserEntity.builder()
                .name(u.getUsername())
                .id(Bytes.fromBase64(u.getPasskeyUserHandle()))
                .displayName(u.getUsername())
                .build();
    }
}
