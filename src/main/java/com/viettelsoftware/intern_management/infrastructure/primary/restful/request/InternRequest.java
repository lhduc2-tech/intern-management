package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternRequest {

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    private UUID userId;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    private Set<UUID> projectIds;

    private UUID internshipSessionId;

    private Boolean graduated;

    @Size(max = 255, message = "Company account must not exceed 255 characters")
    private String companyAccount;

    @Size(max = 255, message = "Technology must not exceed 255 characters")
    private String technology;

    private UUID mentorId;

    @Size(max = 255, message = "Organization must not exceed 255 characters")
    private String organization;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber;

    private LocalDate identityIssueDate;

    @Size(max = 255, message = "Level must not exceed 255 characters")
    private String level;

    @Size(max = 255, message = "Major must not exceed 255 characters")
    private String major;

    @Size(max = 255, message = "University must not exceed 255 characters")
    private String university;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Permanent address must not exceed 255 characters")
    private String permanentAddress;

    @Size(max = 255, message = "Position must not exceed 255 characters")
    private String position;

    private LocalDate internshipStartDate;

    private LocalDate internshipEndDate;

    @Size(max = 255, message = "Acceptance document must not exceed 255 characters")
    private String acceptanceDocument;
}

