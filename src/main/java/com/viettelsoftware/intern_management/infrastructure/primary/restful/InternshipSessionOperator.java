package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.InternshipSessionDTO;
import com.viettelsoftware.intern_management.domain.dto.InternshipSessionSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.InternshipSessionService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreateInternshipSessionRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.InternshipSessionSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateInternshipSessionRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.InternshipSessionResponse;
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
@RequestMapping("${application.path.base.private}/internship-sessions")
@RequiredArgsConstructor
public class InternshipSessionOperator {

    private final InternshipSessionService internshipSessionService;

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public PageResponse<InternshipSessionResponse> searchInternshipSessions(@Valid @RequestBody(required = false) InternshipSessionSearchRequest request) {
        InternshipSessionSearchRequest safeRequest = request == null ? new InternshipSessionSearchRequest() : request;
        return PageResponse.of(
                internshipSessionService.searchInternshipSessions(toCriteria(safeRequest), safeRequest.toPageable()),
                this::toResponse
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR', 'INTERN')")
    public InternshipSessionResponse getInternshipSession(@PathVariable UUID id) {
        return toResponse(internshipSessionService.getInternshipSession(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public InternshipSessionResponse createInternshipSession(@Valid @RequestBody CreateInternshipSessionRequest request) {
        return toResponse(internshipSessionService.createInternshipSession(toCreateDTO(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public InternshipSessionResponse updateInternshipSession(@PathVariable UUID id, @Valid @RequestBody UpdateInternshipSessionRequest request) {
        return toResponse(internshipSessionService.updateInternshipSession(id, toUpdateDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public void deleteInternshipSession(@PathVariable UUID id) {
        internshipSessionService.deleteInternshipSession(id);
    }

    private InternshipSessionDTO toCreateDTO(CreateInternshipSessionRequest request) {
        return InternshipSessionDTO.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    private InternshipSessionDTO toUpdateDTO(UpdateInternshipSessionRequest request) {
        return InternshipSessionDTO.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    private InternshipSessionSearchCriteria toCriteria(InternshipSessionSearchRequest request) {
        return InternshipSessionSearchCriteria.builder()
                .name(request.getName())
                .startFrom(request.getStartFrom())
                .startTo(request.getStartTo())
                .endFrom(request.getEndFrom())
                .endTo(request.getEndTo())
                .build();
    }

    private InternshipSessionResponse toResponse(InternshipSessionDTO dto) {
        return InternshipSessionResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalIntern(dto.getTotalIntern())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

