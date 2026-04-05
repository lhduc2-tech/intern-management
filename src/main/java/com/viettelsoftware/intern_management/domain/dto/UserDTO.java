package com.viettelsoftware.intern_management.domain.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private UUID id;

    private String username;

    private String email;

    private String passwordHash;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String fullName;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}
