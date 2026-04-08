package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TaskAssignmentPort {

    Page<TaskAssignmentDTO> search(TaskAssignmentSearchCriteria criteria, Pageable pageable);

    Optional<TaskAssignmentDTO> findById(UUID id);

    Optional<TaskAssignmentDTO> findByTaskIdAndInternId(UUID taskId, UUID internId);

    TaskAssignmentDTO save(TaskAssignmentDTO taskAssignmentDTO);

    TaskAssignmentDTO update(TaskAssignmentDTO taskAssignmentDTO);

    void deleteById(UUID id);
}

