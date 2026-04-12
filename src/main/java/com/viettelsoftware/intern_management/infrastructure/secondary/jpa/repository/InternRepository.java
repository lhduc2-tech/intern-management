package com.viettelsoftware.intern_management.infrastructure.secondary.jpa.repository;

import com.viettelsoftware.intern_management.domain.entity.InternEntity;
import com.viettelsoftware.intern_management.infrastructure.secondary.jpa.projection.InternSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternRepository extends BaseJPARepository<InternEntity, UUID> {

    Optional<InternEntity> findByUserId(UUID userId);

    Optional<InternEntity> findByCompanyAccount(String companyAccount);

    Optional<InternEntity> findByIdentityNumber(String identityNumber);

    @Query(value = """
            select
                i.id as id,
                i.userId as userId,
                u.fullName as fullName,
                i.status as status,
                i.internshipSessionId as internshipSessionId,
                i.graduated as graduated,
                i.companyAccount as companyAccount,
                i.technology as technology,
                i.mentorId as mentorId,
                i.organization as organization,
                i.gender as gender,
                i.dateOfBirth as dateOfBirth,
                i.identityNumber as identityNumber,
                i.identityIssueDate as identityIssueDate,
                i.level as level,
                i.major as major,
                i.university as university,
                i.phone as phone,
                i.permanentAddress as permanentAddress,
                i.position as position,
                i.internshipStartDate as internshipStartDate,
                i.internshipEndDate as internshipEndDate,
                i.acceptanceDocument as acceptanceDocument,
                i.createdAt as createdAtValue,
                i.createdBy as createdBy,
                i.updatedAt as updatedAtValue,
                i.updatedBy as updatedBy
            from InternEntity i
            left join UserEntity u on u.id = i.userId
            where (:userId is null or i.userId = :userId)
              and (:status is null or i.status = :status)
              and (:projectId is null or exists (
                    select 1
                    from InternProjectEntity ip
                    where ip.internId = i.id
                      and ip.projectId = :projectId
              ))
              and (:internshipSessionId is null or i.internshipSessionId = :internshipSessionId)
              and (:graduated is null or i.graduated = :graduated)
              and lower(coalesce(i.companyAccount, '')) like concat('%', coalesce(:companyAccount, ''), '%')
              and (:mentorId is null or i.mentorId = :mentorId)
              and lower(coalesce(i.organization, '')) like concat('%', coalesce(:organization, ''), '%')
            """,
            countQuery = """
            select count(i)
            from InternEntity i
            where (:userId is null or i.userId = :userId)
              and (:status is null or i.status = :status)
              and (:projectId is null or exists (
                    select 1
                    from InternProjectEntity ip
                    where ip.internId = i.id
                      and ip.projectId = :projectId
              ))
              and (:internshipSessionId is null or i.internshipSessionId = :internshipSessionId)
              and (:graduated is null or i.graduated = :graduated)
              and lower(coalesce(i.companyAccount, '')) like concat('%', coalesce(:companyAccount, ''), '%')
              and (:mentorId is null or i.mentorId = :mentorId)
              and lower(coalesce(i.organization, '')) like concat('%', coalesce(:organization, ''), '%')
            """)
    Page<InternSearchProjection> search(
            @Param("userId") UUID userId,
            @Param("status") String status,
            @Param("projectId") UUID projectId,
            @Param("internshipSessionId") UUID internshipSessionId,
            @Param("graduated") Boolean graduated,
            @Param("companyAccount") String companyAccount,
            @Param("mentorId") UUID mentorId,
            @Param("organization") String organization,
            Pageable pageable
    );

    @Query(value = """
            select
                i.id as id,
                i.userId as userId,
                u.fullName as fullName,
                i.status as status,
                i.internshipSessionId as internshipSessionId,
                i.graduated as graduated,
                i.companyAccount as companyAccount,
                i.technology as technology,
                i.mentorId as mentorId,
                i.organization as organization,
                i.gender as gender,
                i.dateOfBirth as dateOfBirth,
                i.identityNumber as identityNumber,
                i.identityIssueDate as identityIssueDate,
                i.level as level,
                i.major as major,
                i.university as university,
                i.phone as phone,
                i.permanentAddress as permanentAddress,
                i.position as position,
                i.internshipStartDate as internshipStartDate,
                i.internshipEndDate as internshipEndDate,
                i.acceptanceDocument as acceptanceDocument,
                i.createdAt as createdAtValue,
                i.createdBy as createdBy,
                i.updatedAt as updatedAtValue,
                i.updatedBy as updatedBy
            from InternEntity i
            left join UserEntity u on u.id = i.userId
            where i.id = :id
            """)
    Optional<InternSearchProjection> findProjectionById(@Param("id") UUID id);
}

