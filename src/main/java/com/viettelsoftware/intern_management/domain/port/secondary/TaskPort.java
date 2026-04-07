package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.TaskDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TaskPort {

    Page<TaskDTO> search(TaskSearchCriteria criteria, Pageable pageable);

    Optional<TaskDTO> findById(UUID id);

    TaskDTO save(TaskDTO taskDTO);

    TaskDTO update(TaskDTO taskDTO);

    void deleteById(UUID id);
}

