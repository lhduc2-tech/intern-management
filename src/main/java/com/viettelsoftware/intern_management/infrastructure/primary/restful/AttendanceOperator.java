package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.AttendanceDTO;
import com.viettelsoftware.intern_management.domain.dto.AttendanceSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.AttendanceService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.AttendanceRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.AttendanceSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.AttendanceResponse;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${application.path.base.private}/attendances")
@RequiredArgsConstructor
public class AttendanceOperator {

    private final AttendanceService attendanceService;

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'MANAGER', 'INTERN')")
    public PageResponse<AttendanceResponse> searchAttendances(@Valid @RequestBody(required = false) AttendanceSearchRequest request) {
        AttendanceSearchRequest safeRequest = request == null ? new AttendanceSearchRequest() : request;
        return PageResponse.of(attendanceService.searchAttendances(toCriteria(safeRequest), safeRequest.toPageable()), this::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'MANAGER', 'INTERN')")
    public AttendanceResponse getAttendance(@PathVariable UUID id) {
        return toResponse(attendanceService.getAttendance(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'MANAGER', 'INTERN')")
    public AttendanceResponse createAttendance(@Valid @RequestBody AttendanceRequest request) {
        return toResponse(attendanceService.createAttendance(toDTO(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'MANAGER', 'INTERN')")
    public AttendanceResponse updateAttendance(@PathVariable UUID id, @Valid @RequestBody AttendanceRequest request) {
        return toResponse(attendanceService.updateAttendance(id, toDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public void deleteAttendance(@PathVariable UUID id) {
        attendanceService.deleteAttendance(id);
    }

    private AttendanceDTO toDTO(AttendanceRequest request) {
        return AttendanceDTO.builder()
                .internId(request.getInternId())
                .date(request.getDate())
                .note(request.getNote())
                .build();
    }

    private AttendanceSearchCriteria toCriteria(AttendanceSearchRequest request) {
        return AttendanceSearchCriteria.builder()
                .internId(request.getInternId())
                .date(request.getDate())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .build();
    }

    private AttendanceResponse toResponse(AttendanceDTO dto) {
        return AttendanceResponse.builder()
                .id(dto.getId())
                .internId(dto.getInternId())
                .date(dto.getDate())
                .note(dto.getNote())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

