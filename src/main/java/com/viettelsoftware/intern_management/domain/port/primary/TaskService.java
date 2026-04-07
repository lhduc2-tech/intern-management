package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.TaskDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    Page<TaskDTO> searchTasks(TaskSearchCriteria criteria, Pageable pageable);

    TaskDTO getTask(UUID id);

    TaskDTO createTask(TaskDTO taskDTO);

    TaskDTO updateTask(UUID id, TaskDTO taskDTO);

    void deleteTask(UUID id);
}

