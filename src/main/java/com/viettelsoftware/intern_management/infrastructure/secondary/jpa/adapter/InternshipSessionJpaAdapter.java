package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.InternshipSessionDTO;
import com.viettelsoftware.intern_management.domain.dto.InternshipSessionSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.InternshipSessionEntity;
import com.viettelsoftware.intern_management.domain.mapper.InternshipSessionMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.InternshipSessionPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.InternshipSessionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InternshipSessionJpaAdapter extends BaseJPAAdapterVer2<InternshipSessionEntity, InternshipSessionDTO, UUID, InternshipSessionMapper, InternshipSessionRepository> implements InternshipSessionPort {

    public InternshipSessionJpaAdapter(InternshipSessionRepository repository, InternshipSessionMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Page<InternshipSessionDTO> search(InternshipSessionSearchCriteria criteria, Pageable pageable) {
        Specification<InternshipSessionEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<InternshipSessionDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional
    public InternshipSessionDTO save(InternshipSessionDTO internshipSessionDTO) {
        InternshipSessionEntity saved = repository.saveAndFlush(mapper.toEntity(internshipSessionDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public InternshipSessionDTO update(InternshipSessionDTO internshipSessionDTO) {
        InternshipSessionEntity saved = repository.saveAndFlush(mapper.toEntity(internshipSessionDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Specification<InternshipSessionEntity> buildSpecification(InternshipSessionSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + criteria.getName().trim().toLowerCase() + "%"));
            }
            if (criteria.getStartFrom() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), criteria.getStartFrom()));
            }
            if (criteria.getStartTo() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("startDate"), criteria.getStartTo()));
            }
            if (criteria.getEndFrom() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("endDate"), criteria.getEndFrom()));
            }
            if (criteria.getEndTo() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("endDate"), criteria.getEndTo()));
            }

            return predicate;
        };
    }
}

