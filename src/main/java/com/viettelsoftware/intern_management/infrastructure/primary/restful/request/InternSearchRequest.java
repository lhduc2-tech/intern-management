package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class InternSearchRequest extends BaseRequest {

    private UUID userId;

    private String status;

    private UUID projectId;

    private UUID internshipSessionId;

    private Boolean graduated;

    private String companyAccount;

    private UUID mentorId;

    private String organization;
}

