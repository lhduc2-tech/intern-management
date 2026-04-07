package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class InternshipSessionSearchRequest extends BaseRequest {

    private String name;

    private LocalDate startFrom;

    private LocalDate startTo;

    private LocalDate endFrom;

    private LocalDate endTo;
}

