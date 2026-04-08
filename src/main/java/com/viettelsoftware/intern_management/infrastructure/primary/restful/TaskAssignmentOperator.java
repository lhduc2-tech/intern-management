package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.TaskAssignmentService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreateTaskAssignmentRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.TaskAssignmentSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateTaskAssignmentRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.PageResponse;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.TaskAssignmentResponse;
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
@RequestMapping("${application.path.base.private}/task-assignments")
@RequiredArgsConstructor
public class TaskAssignmentOperator {

    private final TaskAssignmentService taskAssignmentService;

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR', 'INTERN')")
    public PageResponse<TaskAssignmentResponse> searchTaskAssignments(@Valid @RequestBody(required = false) TaskAssignmentSearchRequest request) {
        TaskAssignmentSearchRequest safeRequest = request == null ? new TaskAssignmentSearchRequest() : request;
        return PageResponse.of(taskAssignmentService.searchTaskAssignments(toCriteria(safeRequest), safeRequest.toPageable()), this::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR', 'INTERN')")
    public TaskAssignmentResponse getTaskAssignment(@PathVariable UUID id) {
        return toResponse(taskAssignmentService.getTaskAssignment(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public TaskAssignmentResponse createTaskAssignment(@Valid @RequestBody CreateTaskAssignmentRequest request) {
        return toResponse(taskAssignmentService.createTaskAssignment(toCreateDTO(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public TaskAssignmentResponse updateTaskAssignment(@PathVariable UUID id, @Valid @RequestBody UpdateTaskAssignmentRequest request) {
        return toResponse(taskAssignmentService.updateTaskAssignment(id, toUpdateDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public void deleteTaskAssignment(@PathVariable UUID id) {
        taskAssignmentService.deleteTaskAssignment(id);
    }

    private TaskAssignmentDTO toCreateDTO(CreateTaskAssignmentRequest request) {
        return TaskAssignmentDTO.builder()
                .taskId(request.getTaskId())
                .internId(request.getInternId())
                .assignedAt(request.getAssignedAt())
                .build();
    }

    private TaskAssignmentDTO toUpdateDTO(UpdateTaskAssignmentRequest request) {
        return TaskAssignmentDTO.builder()
                .taskId(request.getTaskId())
                .internId(request.getInternId())
                .assignedAt(request.getAssignedAt())
                .build();
    }

    private TaskAssignmentSearchCriteria toCriteria(TaskAssignmentSearchRequest request) {
        return TaskAssignmentSearchCriteria.builder()
                .taskId(request.getTaskId())
                .internId(request.getInternId())
                .build();
    }

    private TaskAssignmentResponse toResponse(TaskAssignmentDTO dto) {
        return TaskAssignmentResponse.builder()
                .id(dto.getId())
                .taskId(dto.getTaskId())
                .internId(dto.getInternId())
                .assignedAt(dto.getAssignedAt())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

