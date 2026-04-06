package com.viettelsoftware.intern_management.domain.port.secondary;

import com.viettelsoftware.intern_management.domain.dto.ProjectDTO;
import com.viettelsoftware.intern_management.domain.dto.ProjectSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProjectPort {

    Page<ProjectDTO> search(ProjectSearchCriteria criteria, Pageable pageable);

    Optional<ProjectDTO> findById(UUID id);

    ProjectDTO save(ProjectDTO projectDTO);

    ProjectDTO update(ProjectDTO projectDTO);

    void deleteById(UUID id);
}

