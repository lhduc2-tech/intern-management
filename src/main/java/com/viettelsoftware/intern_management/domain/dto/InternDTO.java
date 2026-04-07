package com.viettelsoftware.intern_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternDTO {

    private UUID id;

    private UUID userId;

    private String status;

    private UUID projectId;

    private UUID internshipSessionId;

    private Boolean graduated;

    private String companyAccount;

    private String technology;

    private UUID mentorId;

    private String organization;

    private String gender;

    private LocalDate dateOfBirth;

    private String identityNumber;

    private LocalDate identityIssueDate;

    private String level;

    private String major;

    private String university;

    private String phone;

    private String permanentAddress;

    private String position;

    private LocalDate internshipStartDate;

    private LocalDate internshipEndDate;

    private String acceptanceDocument;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}

