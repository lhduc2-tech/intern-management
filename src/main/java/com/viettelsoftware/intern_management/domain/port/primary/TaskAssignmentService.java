package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskAssignmentService {

    Page<TaskAssignmentDTO> searchTaskAssignments(TaskAssignmentSearchCriteria criteria, Pageable pageable);

    TaskAssignmentDTO getTaskAssignment(UUID id);

    TaskAssignmentDTO createTaskAssignment(TaskAssignmentDTO taskAssignmentDTO);

    TaskAssignmentDTO updateTaskAssignment(UUID id, TaskAssignmentDTO taskAssignmentDTO);

    void deleteTaskAssignment(UUID id);
}

