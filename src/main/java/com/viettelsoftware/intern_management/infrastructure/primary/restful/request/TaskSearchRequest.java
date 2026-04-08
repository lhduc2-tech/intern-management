package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TaskSearchRequest extends BaseRequest {

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

