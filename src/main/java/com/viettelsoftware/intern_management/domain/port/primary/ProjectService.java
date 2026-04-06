package com.viettelsoftware.intern_management.domain.port.primary;

import com.viettelsoftware.intern_management.domain.dto.ProjectDTO;
import com.viettelsoftware.intern_management.domain.dto.ProjectSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectService {

    Page<ProjectDTO> searchProjects(ProjectSearchCriteria criteria, Pageable pageable);

    ProjectDTO getProject(UUID id);

    ProjectDTO createProject(ProjectDTO projectDTO);

    ProjectDTO updateProject(UUID id, ProjectDTO projectDTO);

    void deleteProject(UUID id);
}

