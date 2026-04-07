package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.TaskDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.TaskService;
import com.viettelsoftware.intern_management.domain.port.secondary.ProjectPort;
import com.viettelsoftware.intern_management.domain.port.secondary.TaskPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskPort taskPort;
    private final ProjectPort projectPort;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDTO> searchTasks(TaskSearchCriteria criteria, Pageable pageable) {
        TaskSearchCriteria normalized = criteria == null ? new TaskSearchCriteria() : criteria;
        if (normalized.getTitle() != null) {
            normalized.setTitle(normalized.getTitle().trim());
        }
        if (normalized.getPriority() != null) {
            normalized.setPriority(normalized.getPriority().trim().toUpperCase());
        }
        if (normalized.getStatus() != null) {
            normalized.setStatus(normalized.getStatus().trim().toUpperCase());
        }
        validateSearchDateRanges(normalized);
        return taskPort.search(normalized, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDTO getTask(UUID id) {
        return taskPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    @Override
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        if (taskDTO == null) {
            throw new IllegalArgumentException("Task payload must not be null");
        }

        UUID projectId = taskDTO.getProjectId();
        if (projectId == null) {
            throw new IllegalArgumentException("projectId is required");
        }
        validateProjectExists(projectId);
        validateTaskDateRange(taskDTO.getStartDate(), taskDTO.getDueDate());

        TaskDTO toSave = TaskDTO.builder()
                .projectId(projectId)
                .title(normalizeText(taskDTO.getTitle()))
                .description(normalizeText(taskDTO.getDescription()))
                .priority(normalizeUpper(taskDTO.getPriority()))
                .startDate(taskDTO.getStartDate())
                .dueDate(taskDTO.getDueDate())
                .status(defaultStatus(taskDTO.getStatus()))
                .build();
        return taskPort.save(toSave);
    }

    @Override
    @Transactional
    public TaskDTO updateTask(UUID id, TaskDTO taskDTO) {
        TaskDTO existing = getTask(id);
        merge(existing, taskDTO);
        validateProjectExists(existing.getProjectId());
        validateTaskDateRange(existing.getStartDate(), existing.getDueDate());
        return taskPort.update(existing);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        getTask(id);
        taskPort.deleteById(id);
    }

    private void merge(TaskDTO target, TaskDTO source) {
        if (source == null) {
            return;
        }
        if (source.getProjectId() != null) {
            target.setProjectId(source.getProjectId());
        }
        if (source.getTitle() != null) {
            target.setTitle(normalizeText(source.getTitle()));
        }
        if (source.getDescription() != null) {
            target.setDescription(normalizeText(source.getDescription()));
        }
        if (source.getPriority() != null) {
            target.setPriority(normalizeUpper(source.getPriority()));
        }
        if (source.getStartDate() != null) {
            target.setStartDate(source.getStartDate());
        }
        if (source.getDueDate() != null) {
            target.setDueDate(source.getDueDate());
        }
        if (source.getStatus() != null) {
            target.setStatus(defaultStatus(source.getStatus()));
        }
    }

    private void validateProjectExists(UUID projectId) {
        projectPort.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    private void validateTaskDateRange(java.time.LocalDate startDate, java.time.LocalDate dueDate) {
        if (startDate != null && dueDate != null && startDate.isAfter(dueDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to dueDate");
        }
    }

    private void validateSearchDateRanges(TaskSearchCriteria criteria) {
        if (criteria.getStartFrom() != null && criteria.getStartTo() != null && criteria.getStartFrom().isAfter(criteria.getStartTo())) {
            throw new IllegalArgumentException("startFrom must be before or equal to startTo");
        }
        if (criteria.getDueFrom() != null && criteria.getDueTo() != null && criteria.getDueFrom().isAfter(criteria.getDueTo())) {
            throw new IllegalArgumentException("dueFrom must be before or equal to dueTo");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String defaultStatus(String value) {
        String normalized = normalizeUpper(value);
        return (normalized == null || normalized.isBlank()) ? "TODO" : normalized;
    }
}

