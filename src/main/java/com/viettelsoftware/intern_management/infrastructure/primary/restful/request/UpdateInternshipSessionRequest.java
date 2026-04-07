package com.viettelsoftware.intern_management.infrastructure.primary.restful.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInternshipSessionRequest {

    @Size(max = 255, message = "Internship session name must not exceed 255 characters")
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;
}

