package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.ProjectDTO;
import com.viettelsoftware.intern_management.domain.dto.ProjectSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.ProjectService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreateProjectRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.ProjectSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateProjectRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.PageResponse;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.ProjectResponse;
import jakarta.validation.Valid;
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
@RequestMapping("${application.path.base.private}/projects")
public class ProjectOperator {

    private final ProjectService projectService;

    public ProjectOperator(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MENTOR')")
    public PageResponse<ProjectResponse> searchProjects(@Valid @RequestBody(required = false) ProjectSearchRequest request) {
        ProjectSearchRequest safeRequest = request == null ? new ProjectSearchRequest() : request;
        return PageResponse.of(projectService.searchProjects(toCriteria(safeRequest), safeRequest.toPageable()), this::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MENTOR', 'INTERN')")
    public ProjectResponse getProject(@PathVariable UUID id) {
        return toResponse(projectService.getProject(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MENTOR')")
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request) {
        return toResponse(projectService.createProject(toCreateDTO(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MENTOR')")
    public ProjectResponse updateProject(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request) {
        return toResponse(projectService.updateProject(id, toUpdateDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MENTOR')")
    public void deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
    }

    private ProjectDTO toCreateDTO(CreateProjectRequest request) {
        return ProjectDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    private ProjectDTO toUpdateDTO(UpdateProjectRequest request) {
        return ProjectDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    private ProjectSearchCriteria toCriteria(ProjectSearchRequest request) {
        return ProjectSearchCriteria.builder()
                .name(request.getName())
                .build();
    }

    private ProjectResponse toResponse(ProjectDTO dto) {
        return ProjectResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

