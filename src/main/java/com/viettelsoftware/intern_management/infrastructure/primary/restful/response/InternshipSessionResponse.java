package com.viettelsoftware.intern_management.infrastructure.primary.restful.response;

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
public class InternshipSessionResponse {

    private UUID id;

    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalIntern;

    private OffsetDateTime createdAt;

    private String createdBy;

    private OffsetDateTime updatedAt;

    private String updatedBy;
}

