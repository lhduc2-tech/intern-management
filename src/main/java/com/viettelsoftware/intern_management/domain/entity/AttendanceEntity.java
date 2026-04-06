package com.viettelsoftware.intern_management.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.viettelsoftware.intern_management.domain.audit.BaseAuditing;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_intern_date", columnNames = {"intern_id", "date"})
        },
        indexes = {
                @Index(name = "idx_attendance_intern", columnList = "intern_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEntity extends BaseAuditing {

    @Id
    private UUID id;

    @Column(name = "intern_id", nullable = false)
    private UUID internId;

    @Column(name = "date")
    private LocalDate date;

    @Column(columnDefinition = "text")
    private String note;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}

