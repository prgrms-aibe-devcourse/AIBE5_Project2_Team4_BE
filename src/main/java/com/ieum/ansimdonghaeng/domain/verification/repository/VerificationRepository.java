package com.ieum.ansimdonghaeng.domain.verification.repository;

import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

public interface VerificationRepository extends JpaRepository<Verification, Long>, JpaSpecificationExecutor<Verification> {

    long countByStatus(VerificationStatus status);

    boolean existsByFreelancerProfile_IdAndStatus(Long freelancerProfileId, VerificationStatus status);

    @Override
    Page<Verification> findAll(Specification<Verification> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"freelancerProfile", "freelancerProfile.user", "reviewedByUser"})
    List<Verification> findTop5ByStatusOrderByRequestedAtDescIdDesc(VerificationStatus status);

    @Query("""
            select verification
            from Verification verification
            join fetch verification.freelancerProfile freelancerProfile
            join fetch freelancerProfile.user user
            left join fetch verification.reviewedByUser reviewedByUser
            where verification.id = :verificationId
            """)
    Optional<Verification> findDetailById(@Param("verificationId") Long verificationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select verification
            from Verification verification
            join fetch verification.freelancerProfile freelancerProfile
            join fetch freelancerProfile.user user
            left join fetch verification.reviewedByUser reviewedByUser
            where verification.id = :verificationId
            """)
    Optional<Verification> findDetailByIdForUpdate(@Param("verificationId") Long verificationId);

    @Query("""
            select verification
            from Verification verification
            left join fetch verification.reviewedByUser reviewedByUser
            where verification.freelancerProfile.id = :freelancerProfileId
            order by verification.requestedAt desc, verification.id desc
            """)
    List<Verification> findRecentByFreelancerProfileId(@Param("freelancerProfileId") Long freelancerProfileId);
}
