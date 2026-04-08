package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentDTO;
import com.viettelsoftware.intern_management.domain.dto.TaskAssignmentSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.TaskAssignmentEntity;
import com.viettelsoftware.intern_management.domain.mapper.TaskAssignmentMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.TaskAssignmentPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.TaskAssignmentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TaskAssignmentJpaAdapter extends BaseJPAAdapterVer2<TaskAssignmentEntity, TaskAssignmentDTO, UUID, TaskAssignmentMapper, TaskAssignmentRepository> implements TaskAssignmentPort {

    public TaskAssignmentJpaAdapter(TaskAssignmentRepository repository, TaskAssignmentMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Page<TaskAssignmentDTO> search(TaskAssignmentSearchCriteria criteria, Pageable pageable) {
        Specification<TaskAssignmentEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<TaskAssignmentDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<TaskAssignmentDTO> findByTaskIdAndInternId(UUID taskId, UUID internId) {
        return repository.findByTaskIdAndInternId(taskId, internId).map(mapper::toDto);
    }

    @Override
    @Transactional
    public TaskAssignmentDTO save(TaskAssignmentDTO taskAssignmentDTO) {
        TaskAssignmentEntity saved = repository.saveAndFlush(mapper.toEntity(taskAssignmentDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public TaskAssignmentDTO update(TaskAssignmentDTO taskAssignmentDTO) {
        TaskAssignmentEntity saved = repository.saveAndFlush(mapper.toEntity(taskAssignmentDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Specification<TaskAssignmentEntity> buildSpecification(TaskAssignmentSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getTaskId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("taskId"), criteria.getTaskId()));
            }
            if (criteria.getInternId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("internId"), criteria.getInternId()));
            }
            return predicate;
        };
    }
}

