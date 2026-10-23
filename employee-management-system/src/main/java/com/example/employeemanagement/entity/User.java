package com.example.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** BCrypt hash. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    /** Opaque WebAuthn "user handle" (base64url). Created when the user registers their first passkey. */
    @Column(name = "passkey_user_handle", unique = true, length = 128)
    private String passkeyUserHandle;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<PasskeyCredential> passkeys = new ArrayList<>();
}
