package com.viettelsoftware.intern_management.domain.service;

import com.viettelsoftware.intern_management.domain.dto.InternDTO;
import com.viettelsoftware.intern_management.domain.dto.InternSearchCriteria;
import com.viettelsoftware.intern_management.domain.dto.UserDTO;
import com.viettelsoftware.intern_management.domain.entity.RoleEntity;
import com.viettelsoftware.intern_management.domain.port.primary.InternService;
import com.viettelsoftware.intern_management.domain.port.secondary.InternPort;
import com.viettelsoftware.intern_management.domain.port.secondary.InternshipSessionPort;
import com.viettelsoftware.intern_management.domain.port.secondary.PasswordPort;
import com.viettelsoftware.intern_management.domain.port.secondary.RolePort;
import com.viettelsoftware.intern_management.domain.port.secondary.UserPort;
import com.viettelsoftware.intern_management.shared.enumeration.domain.RoleKey;
import com.viettelsoftware.intern_management.shared.enumeration.domain.StatusKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InternServiceImpl implements InternService {

    private final InternPort internPort;
    private final UserPort userPort;
    private final PasswordPort passwordPort;
    private final RolePort rolePort;
    private final InternshipSessionPort internshipSessionPort;

    private static final String INTERN_PASSWORD = "Intern@2026";
    private static final String EMAIL_DOMAIN = "viettelsoftware.com";
    private static final Pattern COMBINING_DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]");

    @Override
    @Transactional(readOnly = true)
    public Page<InternDTO> searchInterns(InternSearchCriteria criteria, Pageable pageable) {
        return internPort.search(normalizeCriteria(criteria), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public InternDTO getIntern(UUID id) {
        return internPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Intern not found: " + id));
    }

    @Override
    @Transactional
    public InternDTO createIntern(InternDTO internDTO, String fullName) {
        InternDTO normalized = normalizeForSave(internDTO);
        UserDTO createdUser = createInternUser(fullName);
        normalized.setUserId(createdUser.getId());
        normalized.setCompanyAccount(createdUser.getEmail());
        validateInternshipSessionExists(normalized.getInternshipSessionId());
        ensureUniqueConstraints(normalized, null);
        return internPort.save(normalized);
    }

    @Override
    @Transactional
    public InternDTO updateIntern(UUID id, InternDTO internDTO) {
        InternDTO existing = getIntern(id);
        merge(existing, internDTO);
        validateInternshipSessionExists(existing.getInternshipSessionId());
        ensureUniqueConstraints(existing, id);
        return internPort.update(existing);
    }

    @Override
    @Transactional
    public void deleteIntern(UUID id) {
        getIntern(id);
        internPort.deleteById(id);
    }

    private InternDTO normalizeForSave(InternDTO internDTO) {
        InternDTO normalized = internDTO == null ? new InternDTO() : internDTO;
        normalized.setId(null);
        normalized.setStatus(normalizeStatus(normalized.getStatus()));
        normalized.setProjectIds(normalizeProjectIds(normalized.getProjectIds()));
        if (normalized.getGraduated() == null) {
            normalized.setGraduated(Boolean.FALSE);
        }
        return normalized;
    }

    private void merge(InternDTO target, InternDTO source) {
        if (source == null) {
            return;
        }
        if (source.getUserId() != null) {
            target.setUserId(source.getUserId());
        }
        if (source.getStatus() != null) {
            target.setStatus(normalizeStatus(source.getStatus()));
        }
        if (source.getProjectIds() != null) {
            target.setProjectIds(normalizeProjectIds(source.getProjectIds()));
        }
        if (source.getInternshipSessionId() != null) {
            target.setInternshipSessionId(source.getInternshipSessionId());
        }
        if (source.getGraduated() != null) {
            target.setGraduated(source.getGraduated());
        }
        if (source.getCompanyAccount() != null) {
            target.setCompanyAccount(source.getCompanyAccount());
        }
        if (source.getTechnology() != null) {
            target.setTechnology(source.getTechnology());
        }
        if (source.getMentorId() != null) {
            target.setMentorId(source.getMentorId());
        }
        if (source.getOrganization() != null) {
            target.setOrganization(source.getOrganization());
        }
        if (source.getGender() != null) {
            target.setGender(source.getGender());
        }
        if (source.getDateOfBirth() != null) {
            target.setDateOfBirth(source.getDateOfBirth());
        }
        if (source.getIdentityNumber() != null) {
            target.setIdentityNumber(source.getIdentityNumber());
        }
        if (source.getIdentityIssueDate() != null) {
            target.setIdentityIssueDate(source.getIdentityIssueDate());
        }
        if (source.getLevel() != null) {
            target.setLevel(source.getLevel());
        }
        if (source.getMajor() != null) {
            target.setMajor(source.getMajor());
        }
        if (source.getUniversity() != null) {
            target.setUniversity(source.getUniversity());
        }
        if (source.getPhone() != null) {
            target.setPhone(source.getPhone());
        }
        if (source.getPermanentAddress() != null) {
            target.setPermanentAddress(source.getPermanentAddress());
        }
        if (source.getPosition() != null) {
            target.setPosition(source.getPosition());
        }
        if (source.getInternshipStartDate() != null) {
            target.setInternshipStartDate(source.getInternshipStartDate());
        }
        if (source.getInternshipEndDate() != null) {
            target.setInternshipEndDate(source.getInternshipEndDate());
        }
        if (source.getAcceptanceDocument() != null) {
            target.setAcceptanceDocument(source.getAcceptanceDocument());
        }
    }

    private void ensureUniqueConstraints(InternDTO internDTO, UUID currentId) {
        if (internDTO.getUserId() != null) {
            internPort.findByUserId(internDTO.getUserId())
                    .filter(found -> currentId == null || !currentId.equals(found.getId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("User already linked to another intern: " + internDTO.getUserId());
                    });
        }
        if (internDTO.getCompanyAccount() != null && !internDTO.getCompanyAccount().isBlank()) {
            internPort.findByCompanyAccount(internDTO.getCompanyAccount())
                    .filter(found -> currentId == null || !currentId.equals(found.getId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Company account already exists: " + internDTO.getCompanyAccount());
                    });
        }
        if (internDTO.getIdentityNumber() != null && !internDTO.getIdentityNumber().isBlank()) {
            internPort.findByIdentityNumber(internDTO.getIdentityNumber())
                    .filter(found -> currentId == null || !currentId.equals(found.getId()))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Identity number already exists: " + internDTO.getIdentityNumber());
                    });
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return StatusKey.ACTIVE.getCode();
        }
        return status.trim().toUpperCase();
    }

    private Set<UUID> normalizeProjectIds(Set<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Set.of();
        }
        return projectIds.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateInternshipSessionExists(UUID internshipSessionId) {
        if (internshipSessionId == null) {
            return;
        }
        internshipSessionPort.findById(internshipSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Internship session not found: " + internshipSessionId));
    }

    private UserDTO createInternUser(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank when creating intern");
        }

        ensureInternRoleExists();
        String username = generateUniqueUsername(fullName);
        String email = username + "@" + EMAIL_DOMAIN;

        UserDTO userDTO = UserDTO.builder()
                .username(username)
                .email(email)
                .fullName(fullName.trim())
                .status(StatusKey.ACTIVE.getCode())
                .roleCodes(Set.of(RoleKey.INTERN.getCode()))
                .passwordHash(passwordPort.encode(INTERN_PASSWORD))
                .build();

        return userPort.save(userDTO);
    }

    private String generateUniqueUsername(String fullName) {
        String base = toUsernameBase(fullName);
        if (base.isBlank()) {
            base = "intern";
        }

        int suffix = 0;
        while (true) {
            String candidate = suffix == 0 ? base : base + suffix;
            String candidateEmail = candidate + "@" + EMAIL_DOMAIN;
            if (!userPort.existsByUsername(candidate) && !userPort.existsByEmail(candidateEmail)) {
                return candidate;
            }
            suffix++;
        }
    }

    private String toUsernameBase(String fullName) {
        String normalized = Normalizer.normalize(fullName.trim(), Normalizer.Form.NFD)
                .replace('đ', 'd')
                .replace('Đ', 'D');
        normalized = COMBINING_DIACRITICS.matcher(normalized).replaceAll("").toLowerCase();

        String[] parts = normalized.split("\\s+");
        if (parts.length == 0) {
            return "intern";
        }

        StringBuilder builder = new StringBuilder();
        String last = sanitizeUsernameToken(parts[parts.length - 1]);
        builder.append(last);

        for (int i = 0; i < parts.length - 1; i++) {
            String token = sanitizeUsernameToken(parts[i]);
            if (!token.isBlank()) {
                builder.append(token.charAt(0));
            }
        }

        return builder.toString();
    }

    private String sanitizeUsernameToken(String token) {
        return NON_ALPHANUMERIC.matcher(token == null ? "" : token).replaceAll("");
    }

    private void ensureInternRoleExists() {
        rolePort.findByCode(RoleKey.INTERN.getCode())
                .orElseGet(() -> rolePort.save(RoleEntity.builder()
                        .code(RoleKey.INTERN.getCode())
                        .name(RoleKey.INTERN.getCode())
                        .description(RoleKey.INTERN.getDescription())
                        .status(StatusKey.ACTIVE.getCode())
                        .build()));
    }

    private InternSearchCriteria normalizeCriteria(InternSearchCriteria criteria) {
        InternSearchCriteria normalized = criteria == null ? new InternSearchCriteria() : criteria;
        if (normalized.getStatus() != null && !normalized.getStatus().isBlank()) {
            normalized.setStatus(normalizeStatus(normalized.getStatus()));
        }
        if (normalized.getCompanyAccount() != null) {
            normalized.setCompanyAccount(normalized.getCompanyAccount().trim().toLowerCase());
        }
        if (normalized.getOrganization() != null) {
            normalized.setOrganization(normalized.getOrganization().trim().toLowerCase());
        }
        return normalized;
    }
}


