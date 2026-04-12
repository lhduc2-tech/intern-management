package com.viettelsoftware.intern_management.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.viettelsoftware.intern_management.domain.audit.BaseAuditing;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "interns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternEntity extends BaseAuditing {

    @Id
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "internship_session_id")
    private UUID internshipSessionId;

    @Column(name = "is_graduated")
    @Builder.Default
    private Boolean graduated = Boolean.FALSE;

    @Column(name = "company_account", unique = true, length = 255)
    private String companyAccount;

    @Column(length = 255)
    private String technology;

    @Column(name = "mentor_id")
    private UUID mentorId;

    @Column(length = 255)
    private String organization;

    @Column(length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "identity_number", unique = true, length = 50)
    private String identityNumber;

    @Column(name = "identity_issue_date")
    private LocalDate identityIssueDate;

    @Column(length = 255)
    private String level;

    @Column(length = 255)
    private String major;

    @Column(length = 255)
    private String university;

    @Column(length = 20)
    private String phone;

    @Column(name = "permanent_address", length = 255)
    private String permanentAddress;

    @Column(length = 255)
    private String position;

    @Column(name = "internship_start_date")
    private LocalDate internshipStartDate;

    @Column(name = "internship_end_date")
    private LocalDate internshipEndDate;

    @Column(name = "acceptance_document", length = 255)
    private String acceptanceDocument;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        if (graduated == null) {
            graduated = Boolean.FALSE;
        }
    }
}

