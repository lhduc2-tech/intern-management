package com.viettelsoftware.intern_management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipSessionSearchCriteria {

    private String name;

    private LocalDate startFrom;

    private LocalDate startTo;

    private LocalDate endFrom;

    private LocalDate endTo;
}

