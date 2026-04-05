package com.viettelsoftware.intern_management.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.viettelsoftware.intern_management.domain.audit.BaseAuditing;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseAuditing {

    @Id
    private UUID id;

    private String username;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    private String status;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "full_name")
    private String fullName;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
