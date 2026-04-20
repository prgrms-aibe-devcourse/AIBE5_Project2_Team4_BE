package com.ieum.ansimdonghaeng.domain.proposal.repository;

import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProposalRepository extends JpaRepository<Proposal, Long>, ProposalQueryRepository {

    boolean existsByProject_IdAndFreelancerProfile_Id(Long projectId, Long freelancerProfileId);

    long countByFreelancerProfile_Id(Long freelancerProfileId);

    long countByFreelancerProfile_IdAndStatus(Long freelancerProfileId, ProposalStatus status);

    List<Proposal> findAllByProject_IdAndStatusAndIdNot(Long projectId, ProposalStatus status, Long proposalId);

    @Query("""
            select proposal
            from Proposal proposal
            join fetch proposal.project project
            join fetch proposal.freelancerProfile freelancerProfile
            join fetch freelancerProfile.user user
            where proposal.project.id = :projectId
              and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
            """)
    Optional<Proposal> findAcceptedProposalByProjectId(@Param("projectId") Long projectId);

    @Query("""
            select proposal
            from Proposal proposal
            join fetch proposal.project project
            join fetch proposal.freelancerProfile freelancerProfile
            join fetch freelancerProfile.user user
            where proposal.project.id in :projectIds
              and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
            """)
    List<Proposal> findAcceptedProposalsByProjectIds(@Param("projectIds") List<Long> projectIds);

    // 상세 응답에서 프로젝트와 프리랜서 정보를 같이 써야 하므로 fetch join으로 조회한다.
    @Query("""
            select proposal
            from Proposal proposal
            join fetch proposal.project project
            join fetch proposal.freelancerProfile freelancerProfile
            join fetch freelancerProfile.user user
            where proposal.id = :proposalId
            """)
    Optional<Proposal> findDetailById(@Param("proposalId") Long proposalId);

    // 제안 수락 시점에는 같은 제안이 동시에 처리되지 않도록 잠금 조회를 사용한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select proposal
            from Proposal proposal
            join fetch proposal.project project
            join fetch proposal.freelancerProfile freelancerProfile
            join fetch freelancerProfile.user user
            where proposal.id = :proposalId
            """)
    Optional<Proposal> findDetailByIdForUpdate(@Param("proposalId") Long proposalId);
}
