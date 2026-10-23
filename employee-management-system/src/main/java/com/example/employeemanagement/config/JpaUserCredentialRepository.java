package com.example.employeemanagement.config;

import com.example.employeemanagement.entity.PasskeyCredential;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.repository.PasskeyCredentialRepository;
import com.example.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Stores Spring Security WebAuthn credential records in the passkey_credentials table. */
@Component
@RequiredArgsConstructor
@Transactional
public class JpaUserCredentialRepository implements UserCredentialRepository {

    private final PasskeyCredentialRepository passkeyRepository;
    private final UserRepository userRepository;

    @Override
    public void save(CredentialRecord record) {
        String credId = record.getCredentialId().toBase64UrlString();
        PasskeyCredential e = passkeyRepository.findByCredentialId(credId).orElseGet(PasskeyCredential::new);
        if (e.getUser() == null) {
            User user = userRepository.findByPasskeyUserHandle(record.getUserEntityUserId().toBase64UrlString())
                    .orElseThrow(() -> new IllegalStateException("No user for passkey user handle"));
            e.setUser(user);
            e.setCredentialId(credId);
        }
        e.setPublicKey(record.getPublicKey().getBytes());
        e.setSignatureCount(record.getSignatureCount());
        e.setUvInitialized(record.isUvInitialized());
        e.setBackupEligible(record.isBackupEligible());
        e.setBackupState(record.isBackupState());
        e.setTransports(record.getTransports() == null ? null
                : record.getTransports().stream().map(AuthenticatorTransport::getValue).collect(Collectors.joining(",")));
        e.setAttestationObject(record.getAttestationObject() == null ? null : record.getAttestationObject().getBytes());
        e.setAttestationClientDataJson(record.getAttestationClientDataJSON() == null ? null : record.getAttestationClientDataJSON().getBytes());
        e.setLabel(record.getLabel());
        e.setCreated(record.getCreated() != null ? record.getCreated() : (e.getCreated() != null ? e.getCreated() : Instant.now()));
        e.setLastUsed(record.getLastUsed());
        passkeyRepository.save(e);
    }

    @Override
    public void delete(Bytes credentialId) {
        passkeyRepository.findByCredentialId(credentialId.toBase64UrlString()).ifPresent(passkeyRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialRecord findByCredentialId(Bytes credentialId) {
        return passkeyRepository.findByCredentialId(credentialId.toBase64UrlString()).map(this::toRecord).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialRecord> findByUserId(Bytes userId) {
        return userRepository.findByPasskeyUserHandle(userId.toBase64UrlString())
                .map(u -> passkeyRepository.findByUserId(u.getId()).stream().map(this::toRecord).toList())
                .orElse(List.of());
    }

    private CredentialRecord toRecord(PasskeyCredential e) {
        Set<AuthenticatorTransport> transports = new LinkedHashSet<>();
        if (e.getTransports() != null && !e.getTransports().isBlank()) {
            for (String t : e.getTransports().split(",")) {
                transports.add(AuthenticatorTransport.valueOf(t.trim()));
            }
        }
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(Bytes.fromBase64(e.getCredentialId()))
                .userEntityUserId(Bytes.fromBase64(e.getUser().getPasskeyUserHandle()))
                .publicKey(new ImmutablePublicKeyCose(e.getPublicKey()))
                .signatureCount(e.getSignatureCount())
                .uvInitialized(e.isUvInitialized())
                .backupEligible(e.isBackupEligible())
                .backupState(e.isBackupState())
                .transports(transports)
                .attestationObject(e.getAttestationObject() == null ? null : new Bytes(e.getAttestationObject()))
                .attestationClientDataJSON(e.getAttestationClientDataJson() == null ? null : new Bytes(e.getAttestationClientDataJson()))
                .created(e.getCreated())
                .lastUsed(e.getLastUsed())
                .label(e.getLabel())
                .build();
    }
}
