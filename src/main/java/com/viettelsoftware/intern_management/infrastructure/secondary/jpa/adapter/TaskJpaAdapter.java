package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.TaskDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.TaskEntity;
import com.viettelsoftware.intern_management.domain.mapper.TaskMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.TaskPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskJpaAdapter extends BaseJPAAdapterVer2<TaskEntity, TaskDTO, UUID, TaskMapper, TaskRepository> implements TaskPort {

    public TaskJpaAdapter(TaskRepository repository, TaskMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Page<TaskDTO> search(TaskSearchCriteria criteria, Pageable pageable) {
        Specification<TaskEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<TaskDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public TaskDTO save(TaskDTO taskDTO) {
        TaskEntity saved = repository.saveAndFlush(mapper.toEntity(taskDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public TaskDTO update(TaskDTO taskDTO) {
        TaskEntity saved = repository.saveAndFlush(mapper.toEntity(taskDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Specification<TaskEntity> buildSpecification(TaskSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getProjectId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("projectId"), criteria.getProjectId()));
            }
            if (criteria.getTitle() != null && !criteria.getTitle().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("title")), "%" + criteria.getTitle().trim().toLowerCase() + "%"));
            }
            if (criteria.getPriority() != null && !criteria.getPriority().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("priority")), criteria.getPriority().trim().toUpperCase()));
            }
            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("status")), criteria.getStatus().trim().toUpperCase()));
            }
            if (criteria.getStartFrom() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), criteria.getStartFrom()));
            }
            if (criteria.getStartTo() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("startDate"), criteria.getStartTo()));
            }
            if (criteria.getDueFrom() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dueDate"), criteria.getDueFrom()));
            }
            if (criteria.getDueTo() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dueDate"), criteria.getDueTo()));
            }

            return predicate;
        };
    }
}

