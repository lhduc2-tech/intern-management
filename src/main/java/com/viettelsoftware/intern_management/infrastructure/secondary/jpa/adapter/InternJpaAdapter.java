package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.adapter;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.dto.InternSearchCriteria;
import com.viettelsoftware.intern_management.domain.entity.InternEntity;
import com.viettelsoftware.intern_management.domain.entity.InternProjectEntity;
import com.viettelsoftware.intern_management.domain.mapper.InternMapper;
import com.viettelsoftware.intern_management.domain.port.secondary.InternPort;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.projection.InternSearchProjection;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.InternProjectRepository;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository.InternRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class InternJpaAdapter extends BaseJPAAdapterVer2<InternEntity, InternDTO, UUID, InternMapper, InternRepository> implements InternPort {

    private final InternProjectRepository internProjectRepository;

    public InternJpaAdapter(InternRepository repository, InternMapper mapper, InternProjectRepository internProjectRepository) {
        super(repository, mapper);
        this.internProjectRepository = internProjectRepository;
    }

    @Override
    public Page<InternDTO> search(InternSearchCriteria criteria, Pageable pageable) {
        InternSearchCriteria safeCriteria = criteria == null ? new InternSearchCriteria() : criteria;
        Pageable effectivePageable = pageable == null ? Pageable.unpaged() : pageable;
        Page<InternDTO> page = repository.search(
                        safeCriteria.getUserId(),
                        safeCriteria.getStatus(),
                        safeCriteria.getProjectId(),
                        safeCriteria.getInternshipSessionId(),
                        safeCriteria.getGraduated(),
                        safeCriteria.getCompanyAccount(),
                        safeCriteria.getMentorId(),
                        safeCriteria.getOrganization(),
                        effectivePageable
                )
                .map(this::toInternSearchResult);
        enrichWithProjects(page.getContent());
        return page;
    }

    @Override
    public Optional<InternDTO> findById(UUID id) {
        return repository.findProjectionById(id).map(this::toInternSearchResult).map(this::enrichWithProjects);
    }

    @Override
    public Optional<InternDTO> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(mapper::toDto).map(this::enrichWithProjects);
    }

    @Override
    public Optional<InternDTO> findByCompanyAccount(String companyAccount) {
        return repository.findByCompanyAccount(companyAccount).map(mapper::toDto).map(this::enrichWithProjects);
    }

    @Override
    public Optional<InternDTO> findByIdentityNumber(String identityNumber) {
        return repository.findByIdentityNumber(identityNumber).map(mapper::toDto).map(this::enrichWithProjects);
    }

    @Override
    public InternDTO save(InternDTO internDTO) {
        InternEntity saved = repository.save(mapper.toEntity(internDTO));
        replaceProjects(saved.getId(), internDTO.getProjectIds());
        return enrichWithProjects(mapper.toDto(saved));
    }

    @Override
    public InternDTO update(InternDTO internDTO) {
        InternEntity saved = repository.save(mapper.toEntity(internDTO));
        replaceProjects(saved.getId(), internDTO.getProjectIds());
        return enrichWithProjects(mapper.toDto(saved));
    }

    @Override
    public void deleteById(UUID id) {
        internProjectRepository.deleteByInternId(id);
        super.delete(id);
    }

    @Override
    public Set<UUID> findProjectIdsByInternId(UUID internId) {
        if (internId == null) {
            return Collections.emptySet();
        }
        return internProjectRepository.findByInternId(internId)
                .stream()
                .map(InternProjectEntity::getProjectId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void replaceProjects(UUID internId, Set<UUID> projectIds) {
        if (internId == null) {
            return;
        }
        internProjectRepository.deleteByInternId(internId);
        Set<UUID> normalizedProjectIds = normalizeProjectIds(projectIds);
        if (normalizedProjectIds.isEmpty()) {
            return;
        }
        List<InternProjectEntity> links = normalizedProjectIds.stream()
                .map(projectId -> InternProjectEntity.builder()
                        .internId(internId)
                        .projectId(projectId)
                        .build())
                .toList();
        internProjectRepository.saveAll(links);
    }

    private InternDTO enrichWithProjects(InternDTO dto) {
        if (dto == null || dto.getId() == null) {
            return dto;
        }
        dto.setProjectIds(findProjectIdsByInternId(dto.getId()));
        return dto;
    }

    private void enrichWithProjects(List<InternDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        Map<UUID, Set<UUID>> projectIdsByIntern = loadProjectIds(dtos.stream()
                .map(InternDTO::getId)
                .filter(java.util.Objects::nonNull)
                .toList());
        dtos.forEach(dto -> dto.setProjectIds(projectIdsByIntern.getOrDefault(dto.getId(), Collections.emptySet())));
    }

    private InternDTO toInternSearchResult(InternSearchProjection projection) {
        if (projection == null) {
            return null;
        }
        return InternDTO.builder()
                .id(projection.getId())
                .userId(projection.getUserId())
                .fullName(projection.getFullName())
                .status(projection.getStatus())
                .internshipSessionId(projection.getInternshipSessionId())
                .graduated(projection.getGraduated())
                .companyAccount(projection.getCompanyAccount())
                .technology(projection.getTechnology())
                .mentorId(projection.getMentorId())
                .organization(projection.getOrganization())
                .gender(projection.getGender())
                .dateOfBirth(projection.getDateOfBirth())
                .identityNumber(projection.getIdentityNumber())
                .identityIssueDate(projection.getIdentityIssueDate())
                .level(projection.getLevel())
                .major(projection.getMajor())
                .university(projection.getUniversity())
                .phone(projection.getPhone())
                .permanentAddress(projection.getPermanentAddress())
                .position(projection.getPosition())
                .internshipStartDate(projection.getInternshipStartDate())
                .internshipEndDate(projection.getInternshipEndDate())
                .acceptanceDocument(projection.getAcceptanceDocument())
                .createdAt(toOffsetDateTime(projection.getCreatedAt()))
                .createdBy(projection.getCreatedBy())
                .updatedAt(toOffsetDateTime(projection.getUpdatedAt()))
                .updatedBy(projection.getUpdatedBy())
                .build();
    }

    private OffsetDateTime toOffsetDateTime(Instant value) {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC);
    }


    private Map<UUID, Set<UUID>> loadProjectIds(Collection<UUID> internIds) {
        if (internIds == null || internIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return internProjectRepository.findByInternIdIn(internIds)
                .stream()
                .collect(Collectors.groupingBy(
                        InternProjectEntity::getInternId,
                        Collectors.mapping(InternProjectEntity::getProjectId, Collectors.toCollection(LinkedHashSet::new))
                ));
    }

    private Set<UUID> normalizeProjectIds(Set<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptySet();
        }
        return projectIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}


