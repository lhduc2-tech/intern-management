package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.projection;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface InternSearchProjection {

    UUID getId();

    UUID getUserId();

    String getFullName();

    String getStatus();

    UUID getInternshipSessionId();

    Boolean getGraduated();

    String getCompanyAccount();

    String getTechnology();

    UUID getMentorId();

    String getOrganization();

    String getGender();

    LocalDate getDateOfBirth();

    String getIdentityNumber();

    LocalDate getIdentityIssueDate();

    String getLevel();

    String getMajor();

    String getUniversity();

    String getPhone();

    String getPermanentAddress();

    String getPosition();

    LocalDate getInternshipStartDate();

    LocalDate getInternshipEndDate();

    String getAcceptanceDocument();

    OffsetDateTime getCreatedAtValue();

    Instant getCreatedAt();

    String getCreatedBy();

    OffsetDateTime getUpdatedAtValue();

    Instant getUpdatedAt();

    String getUpdatedBy();
}

