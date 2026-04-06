package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.ProjectDTO;
import com.viettelsoftware.intern_management.domain.dto.ProjectSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.primary.ProjectService;
import com.viettelsoftware.intern_management.domain.port.secondary.ProjectPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectPort projectPort;

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDTO> searchProjects(ProjectSearchCriteria criteria, Pageable pageable) {
        ProjectSearchCriteria normalized = criteria == null ? new ProjectSearchCriteria() : criteria;
        if (normalized.getName() != null) {
            normalized.setName(normalized.getName().trim());
        }
        return projectPort.search(normalized, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO getProject(UUID id) {
        return projectPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
    }

    @Override
    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        if (projectDTO == null) {
            throw new IllegalArgumentException("Project payload must not be null");
        }
        String name = normalizeRequired(projectDTO.getName(), "Project name must not be blank");
        ProjectDTO toSave = ProjectDTO.builder()
                .name(name)
                .description(normalizeDescription(projectDTO.getDescription()))
                .build();
        return projectPort.save(toSave);
    }

    @Override
    @Transactional
    public ProjectDTO updateProject(UUID id, ProjectDTO projectDTO) {
        ProjectDTO existing = getProject(id);
        merge(existing, projectDTO);
        return projectPort.update(existing);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        getProject(id);
        projectPort.deleteById(id);
    }

    private void merge(ProjectDTO target, ProjectDTO source) {
        if (source == null) {
            return;
        }
        if (source.getName() != null) {
            target.setName(normalizeRequired(source.getName(), "Project name must not be blank"));
        }
        if (source.getDescription() != null) {
            target.setDescription(normalizeDescription(source.getDescription()));
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}

