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
public class AttendanceSearchRequest extends BaseRequest {

    private UUID internId;

    private LocalDate date;

    private LocalDate fromDate;

    private LocalDate toDate;
}

