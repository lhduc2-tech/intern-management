package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.entity.InternEntity;
import com.viettelsoftware.intern_management.domain.mapper.InternMapper;
import com.viettelsoftware.intern_management.domain.dto.InternSearchCriteria;
import com.viettelsoftware.intern_management.domain.port.secondary.InternPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.InternRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InternJpaAdapter extends BaseJPAAdapterVer2<InternEntity, InternDTO, UUID, InternMapper, InternRepository> implements InternPort {

    public InternJpaAdapter(InternRepository repository, InternMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Page<InternDTO> search(InternSearchCriteria criteria, Pageable pageable) {
        Specification<InternEntity> specification = buildSpecification(criteria);
        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public Optional<InternDTO> findById(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<InternDTO> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(mapper::toDto);
    }

    @Override
    public Optional<InternDTO> findByCompanyAccount(String companyAccount) {
        return repository.findByCompanyAccount(companyAccount).map(mapper::toDto);
    }

    @Override
    public Optional<InternDTO> findByIdentityNumber(String identityNumber) {
        return repository.findByIdentityNumber(identityNumber).map(mapper::toDto);
    }

    @Override
    public InternDTO save(InternDTO internDTO) {
        return super.save(internDTO);
    }

    @Override
    public InternDTO update(InternDTO internDTO) {
        return super.update(internDTO);
    }

    @Override
    public void deleteById(UUID id) {
        super.delete(id);
    }

    private Specification<InternEntity> buildSpecification(InternSearchCriteria criteria) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (criteria == null) {
                return predicate;
            }
            if (criteria.getUserId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), criteria.getUserId()));
            }
            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("status")), criteria.getStatus().trim().toUpperCase()));
            }
            if (criteria.getProjectId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("projectId"), criteria.getProjectId()));
            }
            if (criteria.getInternshipSessionId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("internshipSessionId"), criteria.getInternshipSessionId()));
            }
            if (criteria.getGraduated() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("graduated"), criteria.getGraduated()));
            }
            if (criteria.getCompanyAccount() != null && !criteria.getCompanyAccount().isBlank()) {
                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("companyAccount")),
                                "%" + criteria.getCompanyAccount().trim().toLowerCase() + "%"
                        )
                );
            }
            if (criteria.getMentorId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("mentorId"), criteria.getMentorId()));
            }
            if (criteria.getOrganization() != null && !criteria.getOrganization().isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("organization")), "%" + criteria.getOrganization().trim().toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}


