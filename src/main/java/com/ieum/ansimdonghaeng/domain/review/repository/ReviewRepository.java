package com.ieum.ansimdonghaeng.domain.review.repository;

import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    long countByBlindedYn(String blindedYn);

    long countByReviewerUserId(Long reviewerUserId);

    @Override
    Page<Review> findAll(Specification<Review> spec, Pageable pageable);

    boolean existsByProject_Id(Long projectId);

    @EntityGraph(attributePaths = {"project", "reviewerUser", "tags"})
    Optional<Review> findByProject_Id(Long projectId);

    @EntityGraph(attributePaths = {"project", "project.ownerUser", "reviewerUser", "tags"})
    Page<Review> findAllByReviewerUserIdOrderByCreatedAtDescIdDesc(Long reviewerUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "project.ownerUser", "reviewerUser", "tags"})
    @Query("""
            select review
            from Review review
            join review.project project
            join com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal proposal
              on proposal.project = project
            join proposal.freelancerProfile freelancerProfile
            join freelancerProfile.user user
            where proposal.freelancerProfile.id = :freelancerProfileId
              and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
              and freelancerProfile.publicYn = true
              and user.activeYn = true
              and user.roleCode = 'ROLE_FREELANCER'
              and review.blindedYn = 'N'
            order by review.createdAt desc, review.id desc
            """)
    Page<Review> findPublicReviewsByFreelancerProfileId(@Param("freelancerProfileId") Long freelancerProfileId,
                                                        Pageable pageable);

    @EntityGraph(attributePaths = {"project", "project.ownerUser", "reviewerUser", "tags"})
    @Query("""
            select review
            from Review review
            where review.id = :reviewId
            """)
    Optional<Review> findDetailById(@Param("reviewId") Long reviewId);
}
