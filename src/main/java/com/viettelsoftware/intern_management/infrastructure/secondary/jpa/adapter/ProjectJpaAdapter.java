package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.ProjectDTO;
import com.viettelsoftware.intern_management.domain.dto.ProjectSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.ProjectEntity;
import com.viettelsoftware.intern_management.domain.mapper.ProjectMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.ProjectPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.ProjectRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ProjectJpaAdapter extends BaseJPAAdapterVer2<ProjectEntity, ProjectDTO, UUID, ProjectMapper, ProjectRepository> implements ProjectPort {

    public ProjectJpaAdapter(ProjectRepository repository, ProjectMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Page<ProjectDTO> search(ProjectSearchCriteria criteria, Pageable pageable) {
        Specification<ProjectEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<ProjectDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public ProjectDTO save(ProjectDTO projectDTO) {
        ProjectEntity saved = repository.saveAndFlush(mapper.toEntity(projectDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public ProjectDTO update(ProjectDTO projectDTO) {
        ProjectEntity saved = repository.saveAndFlush(mapper.toEntity(projectDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Specification<ProjectEntity> buildSpecification(ProjectSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + criteria.getName().trim().toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}

