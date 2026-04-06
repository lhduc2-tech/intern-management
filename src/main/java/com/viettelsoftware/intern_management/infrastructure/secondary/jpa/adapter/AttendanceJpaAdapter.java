package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.AttendanceDTO;
import com.viettelsoftware.intern_management.domain.dto.AttendanceSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.AttendanceEntity;
import com.viettelsoftware.intern_management.domain.mapper.AttendanceMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.AttendancePort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.AttendanceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AttendanceJpaAdapter extends BaseJPAAdapterVer2<AttendanceEntity, AttendanceDTO, UUID, AttendanceMapper, AttendanceRepository> implements AttendancePort {

    public AttendanceJpaAdapter(AttendanceRepository repository, AttendanceMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Page<AttendanceDTO> search(AttendanceSearchCriteria criteria, Pageable pageable) {
        Specification<AttendanceEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<AttendanceDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<AttendanceDTO> findByInternIdAndDate(UUID internId, LocalDate date) {
        return repository.findByInternIdAndDate(internId, date).map(mapper::toDto);
    }

    @Override
    public AttendanceDTO save(AttendanceDTO attendanceDTO) {
        AttendanceEntity saved = repository.saveAndFlush(mapper.toEntity(attendanceDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    public AttendanceDTO update(AttendanceDTO attendanceDTO) {
        AttendanceEntity saved = repository.saveAndFlush(mapper.toEntity(attendanceDTO));
        return repository.findById(saved.getId()).map(mapper::toDto).orElseGet(() -> mapper.toDto(saved));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Specification<AttendanceEntity> buildSpecification(AttendanceSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getInternId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("internId"), criteria.getInternId()));
            }
            if (criteria.getDate() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("date"), criteria.getDate()));
            }
            if (criteria.getFromDate() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("date"), criteria.getFromDate()));
            }
            if (criteria.getToDate() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("date"), criteria.getToDate()));
            }
            return predicate;
        };
    }
}

