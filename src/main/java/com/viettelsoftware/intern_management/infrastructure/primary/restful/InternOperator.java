package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.dto.InternSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.InternService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.InternRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.InternSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.InternResponse;
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
@RequestMapping("${application.path.base.private}/interns")
@RequiredArgsConstructor
public class InternOperator {

    private final InternService internService;

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'MANAGER')")
    public PageResponse<InternResponse> searchInterns(@Valid @RequestBody(required = false) InternSearchRequest request) {
        InternSearchRequest safeRequest = request == null ? new InternSearchRequest() : request;
        return PageResponse.of(internService.searchInterns(toCriteria(safeRequest), safeRequest.toPageable()), this::toInternResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MENTOR', 'MANAGER', 'INTERN')")
    public InternResponse getIntern(@PathVariable UUID id) {
        return toInternResponse(internService.getIntern(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public InternResponse createIntern(@Valid @RequestBody InternRequest request) {
        return toInternResponse(internService.createIntern(toInternDTO(request), request.getFullName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'INTERN')")
    public InternResponse updateIntern(@PathVariable UUID id, @Valid @RequestBody InternRequest request) {
        return toInternResponse(internService.updateIntern(id, toInternDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public void deleteIntern(@PathVariable UUID id) {
        internService.deleteIntern(id);
    }

    private InternDTO toInternDTO(InternRequest request) {
        return InternDTO.builder()
                .userId(request.getUserId())
                .status(request.getStatus())
                .projectId(request.getProjectId())
                .internshipSessionId(request.getInternshipSessionId())
                .graduated(request.getGraduated())
                .companyAccount(request.getCompanyAccount())
                .technology(request.getTechnology())
                .mentorId(request.getMentorId())
                .organization(request.getOrganization())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .identityNumber(request.getIdentityNumber())
                .identityIssueDate(request.getIdentityIssueDate())
                .level(request.getLevel())
                .major(request.getMajor())
                .university(request.getUniversity())
                .phone(request.getPhone())
                .permanentAddress(request.getPermanentAddress())
                .position(request.getPosition())
                .internshipStartDate(request.getInternshipStartDate())
                .internshipEndDate(request.getInternshipEndDate())
                .acceptanceDocument(request.getAcceptanceDocument())
                .build();
    }

    private InternSearchCriteria toCriteria(InternSearchRequest request) {
        return InternSearchCriteria.builder()
                .userId(request.getUserId())
                .status(request.getStatus())
                .projectId(request.getProjectId())
                .internshipSessionId(request.getInternshipSessionId())
                .graduated(request.getGraduated())
                .companyAccount(request.getCompanyAccount())
                .mentorId(request.getMentorId())
                .organization(request.getOrganization())
                .build();
    }

    private InternResponse toInternResponse(InternDTO dto) {
        return InternResponse.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .status(dto.getStatus())
                .projectId(dto.getProjectId())
                .internshipSessionId(dto.getInternshipSessionId())
                .graduated(dto.getGraduated())
                .companyAccount(dto.getCompanyAccount())
                .technology(dto.getTechnology())
                .mentorId(dto.getMentorId())
                .organization(dto.getOrganization())
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .identityNumber(dto.getIdentityNumber())
                .identityIssueDate(dto.getIdentityIssueDate())
                .level(dto.getLevel())
                .major(dto.getMajor())
                .university(dto.getUniversity())
                .phone(dto.getPhone())
                .permanentAddress(dto.getPermanentAddress())
                .position(dto.getPosition())
                .internshipStartDate(dto.getInternshipStartDate())
                .internshipEndDate(dto.getInternshipEndDate())
                .acceptanceDocument(dto.getAcceptanceDocument())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

