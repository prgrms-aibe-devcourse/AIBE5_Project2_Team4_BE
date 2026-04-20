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

    long countByReviewerUser_Id(Long reviewerUserId);

    @Override
    Page<Review> findAll(Specification<Review> spec, Pageable pageable);

    boolean existsByProject_Id(Long projectId);

    @EntityGraph(attributePaths = {"project", "reviewerUser"})
    Optional<Review> findByProject_Id(Long projectId);

    @EntityGraph(attributePaths = {"project", "reviewerUser"})
    Optional<Review> findById(Long reviewId);

    @EntityGraph(attributePaths = {"project", "reviewerUser"})
    Page<Review> findAllByReviewerUser_IdOrderByCreatedAtDescIdDesc(Long reviewerUserId, Pageable pageable);

    @Query(value = """
            select review
            from Review review
            join review.project project
            where review.blindedYn = 'N'
              and exists (
                  select 1
                  from Proposal proposal
                  where proposal.project = project
                    and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
                    and proposal.freelancerProfile.id = :freelancerProfileId
              )
            order by review.createdAt desc, review.id desc
            """,
            countQuery = """
                    select count(review)
                    from Review review
                    join review.project project
                    where review.blindedYn = 'N'
                      and exists (
                          select 1
                          from Proposal proposal
                          where proposal.project = project
                            and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
                            and proposal.freelancerProfile.id = :freelancerProfileId
                      )
                    """)
    @EntityGraph(attributePaths = {"project", "reviewerUser"})
    Page<Review> findVisibleByFreelancerProfileId(@Param("freelancerProfileId") Long freelancerProfileId, Pageable pageable);

    @Query("""
            select count(review)
            from Review review
            join review.project project
            where review.blindedYn = 'N'
              and exists (
                  select 1
                  from Proposal proposal
                  where proposal.project = project
                    and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
                    and proposal.freelancerProfile.id = :freelancerProfileId
              )
            """)
    long countVisibleByFreelancerProfileId(@Param("freelancerProfileId") Long freelancerProfileId);

    @Query("""
            select avg(review.rating)
            from Review review
            join review.project project
            where review.blindedYn = 'N'
              and exists (
                  select 1
                  from Proposal proposal
                  where proposal.project = project
                    and proposal.status = com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus.ACCEPTED
                    and proposal.freelancerProfile.id = :freelancerProfileId
              )
            """)
    Double averageRatingByFreelancerProfileId(@Param("freelancerProfileId") Long freelancerProfileId);
}
