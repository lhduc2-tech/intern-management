package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import com.viettelsoftware.intern_management.domain.dto.TaskDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.TaskService;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.CreateTaskRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.TaskSearchRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.request.UpdateTaskRequest;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.PageResponse;
import com.viettelsoftware.intern_management.infrastructure.primary.restful.response.TaskResponse;
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
@RequestMapping("${application.path.base.private}/tasks")
public class TaskOperator {

    private final TaskService taskService;

    public TaskOperator(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR', 'INTERN')")
    public PageResponse<TaskResponse> searchTasks(@Valid @RequestBody(required = false) TaskSearchRequest request) {
        TaskSearchRequest safeRequest = request == null ? new TaskSearchRequest() : request;
        return PageResponse.of(taskService.searchTasks(toCriteria(safeRequest), safeRequest.toPageable()), this::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR', 'INTERN')")
    public TaskResponse getTask(@PathVariable UUID id) {
        return toResponse(taskService.getTask(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        return toResponse(taskService.createTask(toCreateDTO(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    public TaskResponse updateTask(@PathVariable UUID id, @Valid @RequestBody UpdateTaskRequest request) {
        return toResponse(taskService.updateTask(id, toUpdateDTO(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
    }

    private TaskDTO toCreateDTO(CreateTaskRequest request) {
        return TaskDTO.builder()
                .projectId(request.getProjectId())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .build();
    }

    private TaskDTO toUpdateDTO(UpdateTaskRequest request) {
        return TaskDTO.builder()
                .projectId(request.getProjectId())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .build();
    }

    private TaskSearchCriteria toCriteria(TaskSearchRequest request) {
        return TaskSearchCriteria.builder()
                .projectId(request.getProjectId())
                .title(request.getTitle())
                .priority(request.getPriority())
                .status(request.getStatus())
                .startFrom(request.getStartFrom())
                .startTo(request.getStartTo())
                .dueFrom(request.getDueFrom())
                .dueTo(request.getDueTo())
                .build();
    }

    private TaskResponse toResponse(TaskDTO dto) {
        return TaskResponse.builder()
                .id(dto.getId())
                .projectId(dto.getProjectId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .startDate(dto.getStartDate())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}

