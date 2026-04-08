package com.viettelsoftware.intern_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchCriteria {

    private UUID projectId;

    private UUID internId;

    private String title;

    private String priority;

    private String status;

    private LocalDate startFrom;

    private LocalDate startTo;

    private LocalDate dueFrom;

    private LocalDate dueTo;
}

