package com.ieum.ansimdonghaeng.domain.verification.repository;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationRequest;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

    @Query("""
            select request
            from VerificationRequest request
            join fetch request.freelancerProfile profile
            join fetch profile.user
            where (:status is null or request.status = :status)
            """)
    Page<VerificationRequest> findAllWithProfile(@Param("status") VerificationStatus status, Pageable pageable);

    boolean existsByFreelancerProfile_IdAndTypeAndStatus(Long freelancerProfileId,
                                                         VerificationType type,
                                                         VerificationStatus status);

    @Query("""
            select request
            from VerificationRequest request
            join fetch request.freelancerProfile profile
            join fetch profile.user
            where profile.user.id = :userId
            order by request.createdAt desc
            """)
    List<VerificationRequest> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            select request
            from VerificationRequest request
            join fetch request.freelancerProfile profile
            join fetch profile.user
            where request.id = :requestId
            """)
    Optional<VerificationRequest> findDetailById(@Param("requestId") Long requestId);
}
