package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** A WebAuthn credential (passkey / biometric authenticator) registered by a user. */
@Entity
@Table(name = "passkey_credentials")
@Getter
@Setter
@NoArgsConstructor
public class PasskeyCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** base64url credential id. */
    @Column(name = "credential_id", nullable = false, unique = true, length = 512)
    private String credentialId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** COSE-encoded public key. */
    @Lob
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;

    private long signatureCount;
    private boolean uvInitialized;
    private boolean backupEligible;
    private boolean backupState;

    /** Comma separated transports (usb,nfc,internal,hybrid...). */
    private String transports;

    @Lob
    private byte[] attestationObject;

    @Lob
    private byte[] attestationClientDataJson;

    private String label;
    private Instant created;
    private Instant lastUsed;

    public LocalDateTime getCreatedAt() {
        return created == null ? null : LocalDateTime.ofInstant(created, ZoneId.systemDefault());
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsed == null ? null : LocalDateTime.ofInstant(lastUsed, ZoneId.systemDefault());
    }
}
