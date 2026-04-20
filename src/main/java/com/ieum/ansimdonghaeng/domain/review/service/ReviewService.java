package com.ieum.ansimdonghaeng.domain.review.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewCreateRequest;
import com.ieum.ansimdonghaeng.domain.review.dto.response.MyReviewListResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.MyReviewResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewUpdateRequest;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewAggregateResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewDeleteResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewEligibilityResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewListResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewResponse;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReviewResponse createReview(Long currentUserId, Long projectId, ReviewCreateRequest request) {
        User reviewer = getActiveUser(currentUserId);
        Project project = getOwnedProject(projectId, currentUserId);
        validateReviewEligibility(project, projectId);
        requireAcceptedProposal(projectId);

        Review review = Review.create(project, reviewer, request.rating(), request.content());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(Long currentUserId, Long reviewId, ReviewUpdateRequest request) {
        Review review = getOwnedReview(reviewId, currentUserId);
        review.update(request.rating(), request.content());
        return ReviewResponse.from(review);
    }

    public ReviewDeleteResponse deleteReview(Long currentUserId, Long reviewId) {
        Review review = getOwnedReview(reviewId, currentUserId);
        reviewRepository.delete(review);
        return new ReviewDeleteResponse(reviewId, true);
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getFreelancerReviews(Long freelancerProfileId, Pageable pageable) {
        var page = reviewRepository.findVisibleByFreelancerProfileId(freelancerProfileId, pageable);
        return ReviewListResponse.from(
                page,
                ReviewAggregateResponse.of(
                        freelancerProfileId,
                        reviewRepository.averageRatingByFreelancerProfileId(freelancerProfileId),
                        reviewRepository.countVisibleByFreelancerProfileId(freelancerProfileId)
                )
        );
    }

    @Transactional(readOnly = true)
    public MyReviewListResponse getMyReviews(Long currentUserId, Pageable pageable) {
        getActiveUser(currentUserId);
        var page = reviewRepository.findAllByReviewerUser_IdOrderByCreatedAtDescIdDesc(currentUserId, pageable);
        if (page.isEmpty()) {
            return MyReviewListResponse.from(new PageImpl<>(Collections.emptyList(), pageable, 0));
        }

        var projectIds = page.getContent().stream()
                .map(review -> review.getProject().getId())
                .distinct()
                .toList();
        Map<Long, Proposal> acceptedProposalByProjectId = proposalRepository.findAcceptedProposalsByProjectIds(projectIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        proposal -> proposal.getProject().getId(),
                        Function.identity()
                ));
        Set<Long> reportedReviewIds = reportRepository.findReportedReviewIds(
                page.getContent().stream().map(Review::getId).toList()
        );

        var content = page.getContent().stream()
                .map(review -> MyReviewResponse.from(
                        review,
                        acceptedProposalByProjectId.get(review.getProject().getId()),
                        reportedReviewIds.contains(review.getId())
                ))
                .toList();

        return MyReviewListResponse.from(new PageImpl<>(content, pageable, page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public ReviewEligibilityResponse getReviewEligibility(Long currentUserId, Long projectId) {
        Project project = getOwnedProject(projectId, currentUserId);
        if (project.getStatus() != ProjectStatus.COMPLETED) {
            return new ReviewEligibilityResponse(projectId, false, "PROJECT_NOT_COMPLETED", null);
        }

        Review existingReview = reviewRepository.findByProject_Id(projectId).orElse(null);
        if (existingReview != null) {
            return new ReviewEligibilityResponse(projectId, false, "REVIEW_ALREADY_EXISTS", existingReview.getId());
        }

        if (proposalRepository.findAcceptedProposalByProjectId(projectId).isEmpty()) {
            return new ReviewEligibilityResponse(projectId, false, "ACCEPTED_PROPOSAL_NOT_FOUND", null);
        }

        return new ReviewEligibilityResponse(projectId, true, "OK", null);
    }

    private Project getOwnedProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        return project;
    }

    private Review getOwnedReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getReviewerUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }
        return review;
    }

    private User getActiveUser(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));
        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }
        return user;
    }

    private void validateReviewEligibility(Project project, Long projectId) {
        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new CustomException(ErrorCode.REVIEW_NOT_ELIGIBLE, "Review can only be created for completed projects.");
        }

        if (reviewRepository.existsByProject_Id(projectId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    private Proposal requireAcceptedProposal(Long projectId) {
        return proposalRepository.findAcceptedProposalByProjectId(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_ELIGIBLE, "Accepted proposal was not found."));
    }
}
