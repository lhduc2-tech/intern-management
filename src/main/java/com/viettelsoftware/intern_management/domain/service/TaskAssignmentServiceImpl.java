package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.TaskAssignmentService;
import com.viettelsoftware.intern_management.domain.port.secondary.InternPort;
import com.viettelsoftware.intern_management.domain.port.secondary.TaskAssignmentPort;
import com.viettelsoftware.intern_management.domain.port.secondary.TaskPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    private final TaskAssignmentPort taskAssignmentPort;
    private final TaskPort taskPort;
    private final InternPort internPort;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskAssignmentDTO> searchTaskAssignments(TaskAssignmentSearchCriteria criteria, Pageable pageable) {
        TaskAssignmentSearchCriteria normalized = criteria == null ? new TaskAssignmentSearchCriteria() : criteria;
        return taskAssignmentPort.search(normalized, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAssignmentDTO getTaskAssignment(UUID id) {
        return taskAssignmentPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task assignment not found: " + id));
    }

    @Override
    @Transactional
    public TaskAssignmentDTO createTaskAssignment(TaskAssignmentDTO taskAssignmentDTO) {
        if (taskAssignmentDTO == null) {
            throw new IllegalArgumentException("Task assignment payload must not be null");
        }
        validateRequired(taskAssignmentDTO.getTaskId(), taskAssignmentDTO.getInternId());
        validateTaskExists(taskAssignmentDTO.getTaskId());
        validateInternExists(taskAssignmentDTO.getInternId());
        ensureUniqueTaskIntern(taskAssignmentDTO.getTaskId(), taskAssignmentDTO.getInternId(), null);

        taskAssignmentDTO.setId(null);
        if (taskAssignmentDTO.getAssignedAt() == null) {
            taskAssignmentDTO.setAssignedAt(OffsetDateTime.now());
        }
        return taskAssignmentPort.save(taskAssignmentDTO);
    }

    @Override
    @Transactional
    public TaskAssignmentDTO updateTaskAssignment(UUID id, TaskAssignmentDTO taskAssignmentDTO) {
        TaskAssignmentDTO existing = getTaskAssignment(id);
        merge(existing, taskAssignmentDTO);
        validateRequired(existing.getTaskId(), existing.getInternId());
        validateTaskExists(existing.getTaskId());
        validateInternExists(existing.getInternId());
        ensureUniqueTaskIntern(existing.getTaskId(), existing.getInternId(), id);

        return taskAssignmentPort.update(existing);
    }

    @Override
    @Transactional
    public void deleteTaskAssignment(UUID id) {
        getTaskAssignment(id);
        taskAssignmentPort.deleteById(id);
    }

    private void validateRequired(UUID taskId, UUID internId) {
        if (taskId == null) {
            throw new IllegalArgumentException("taskId is required");
        }
        if (internId == null) {
            throw new IllegalArgumentException("internId is required");
        }
    }

    private void validateTaskExists(UUID taskId) {
        taskPort.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private void validateInternExists(UUID internId) {
        internPort.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Intern not found: " + internId));
    }

    private void ensureUniqueTaskIntern(UUID taskId, UUID internId, UUID currentId) {
        taskAssignmentPort.findByTaskIdAndInternId(taskId, internId)
                .filter(found -> currentId == null || !currentId.equals(found.getId()))
                .ifPresent(found -> {
                    throw new IllegalArgumentException("Task assignment already exists for task and intern");
                });
    }

    private void merge(TaskAssignmentDTO target, TaskAssignmentDTO source) {
        if (source == null) {
            return;
        }
        if (source.getTaskId() != null) {
            target.setTaskId(source.getTaskId());
        }
        if (source.getInternId() != null) {
            target.setInternId(source.getInternId());
        }
        if (source.getAssignedAt() != null) {
            target.setAssignedAt(source.getAssignedAt());
        }
    }
}

