package com.viettelsoftware.intern_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class InternSearchCriteria {

    private UUID userId;

    private String status;

    private UUID projectId;

    private Boolean graduated;

    private String companyAccount;

    private UUID mentorId;

    private String organization;
}


