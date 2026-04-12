package com.viettelsoftware.intern_management.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "intern_project",
        indexes = {
                @Index(name = "idx_intern_project_intern", columnList = "intern_id"),
                @Index(name = "idx_intern_project_project", columnList = "project_id")
        }
)
@IdClass(InternProjectId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternProjectEntity {

    @Id
    @Column(name = "intern_id", nullable = false)
    private UUID internId;

    @Id
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(name = "role", length = 100)
    private String role;

    @PrePersist
    public void prePersist() {
        if (joinedAt == null) {
            joinedAt = OffsetDateTime.now();
        }
    }
}

