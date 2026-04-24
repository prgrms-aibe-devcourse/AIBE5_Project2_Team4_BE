package com.ieum.ansimdonghaeng.domain.review.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerStatsService;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewCreateRequest;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewUpdateRequest;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewDeleteResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewDetailResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewSummaryResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewTagCodeResponse;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewTagCodeRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final ReportRepository reportRepository;
    private final ReviewTagCodeRepository reviewTagCodeRepository;
    private final FreelancerService freelancerService;
    private final FreelancerStatsService freelancerStatsService;

    @Transactional
    public ReviewDetailResponse createReview(Long currentUserId, Long projectId, ReviewCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        Proposal acceptedProposal = getAcceptedProposal(projectId);
        return createParticipantReview(projectId, project, currentUserId, request, acceptedProposal);
    }

    @Transactional
    public ReviewDetailResponse createRequesterReview(Long currentUserId, Long projectId, ReviewCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        Proposal acceptedProposal = getAcceptedProposal(projectId);

        Long assignedFreelancerUserId = acceptedProposal.getFreelancerProfile().getUser().getId();
        if (!assignedFreelancerUserId.equals(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return createParticipantReview(projectId, project, currentUserId, request, acceptedProposal);
    }

    private ReviewDetailResponse createParticipantReview(Long projectId,
                                                         Project project,
                                                         Long reviewerUserId,
                                                         ReviewCreateRequest request,
                                                         Proposal acceptedProposal) {
        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new CustomException(ErrorCode.REVIEW_NOT_ELIGIBLE);
        }
        if (reviewRepository.existsByProject_IdAndReviewerUserId(projectId, reviewerUserId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review savedReview;
        try {
            savedReview = reviewRepository.saveAndFlush(
                    Review.create(
                            project,
                            reviewerUserId,
                            request.rating(),
                            request.content(),
                            normalizeTagCodes(request.tagCodes())
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        freelancerStatsService.refreshStats(acceptedProposal.getFreelancerProfile().getId());
        Review detailReview = reviewRepository.findDetailById(savedReview.getId()).orElse(savedReview);
        return ReviewDetailResponse.from(detailReview, acceptedProposal, false);
    }

    public PageResponse<ReviewSummaryResponse> getMyReviews(Long currentUserId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAllByReviewerUserIdOrderByCreatedAtDescIdDesc(currentUserId, pageable);
        if (reviewPage.isEmpty()) {
            return PageResponse.from(reviewPage.map(ReviewSummaryResponse::from));
        }

        List<Long> projectIds = reviewPage.getContent().stream()
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
                reviewPage.getContent().stream().map(Review::getId).toList()
        );

        return PageResponse.from(reviewPage.map(review -> ReviewSummaryResponse.from(
                review,
                acceptedProposalByProjectId.get(review.getProject().getId()),
                reportedReviewIds.contains(review.getId())
        )));
    }

    public ReviewDetailResponse getMyReview(Long currentUserId, Long reviewId) {
        Review review = getOwnedReview(reviewId, currentUserId);
        Proposal acceptedProposal = proposalRepository.findAcceptedProposalByProjectId(review.getProject().getId()).orElse(null);
        boolean reported = reportRepository.existsByReview_Id(reviewId);
        return ReviewDetailResponse.from(review, acceptedProposal, reported);
    }

    @Transactional
    public ReviewDetailResponse updateMyReview(Long currentUserId, Long reviewId, ReviewUpdateRequest request) {
        Review review = getOwnedReview(reviewId, currentUserId);
        review.update(request.rating(), request.content(), normalizeTagCodes(request.tagCodes()));

        Proposal acceptedProposal = proposalRepository.findAcceptedProposalByProjectId(review.getProject().getId()).orElse(null);
        if (acceptedProposal != null) {
            freelancerStatsService.refreshStats(acceptedProposal.getFreelancerProfile().getId());
        }
        boolean reported = reportRepository.existsByReview_Id(reviewId);
        return ReviewDetailResponse.from(review, acceptedProposal, reported);
    }

    @Transactional
    public ReviewDeleteResponse deleteMyReview(Long currentUserId, Long reviewId) {
        Review review = getOwnedReview(reviewId, currentUserId);
        Proposal acceptedProposal = proposalRepository.findAcceptedProposalByProjectId(review.getProject().getId()).orElse(null);
        reviewRepository.delete(review);
        if (acceptedProposal != null) {
            freelancerStatsService.refreshStats(acceptedProposal.getFreelancerProfile().getId());
        }
        return new ReviewDeleteResponse(reviewId, true);
    }

    public PageResponse<ReviewSummaryResponse> getPublicFreelancerReviews(Long freelancerProfileId, Pageable pageable) {
        freelancerService.getPublicFreelancerProfile(freelancerProfileId);
        Page<Review> reviewPage = reviewRepository.findPublicReviewsByFreelancerProfileId(freelancerProfileId, pageable);
        if (reviewPage.isEmpty()) {
            return PageResponse.from(reviewPage.map(ReviewSummaryResponse::from));
        }

        List<Long> projectIds = reviewPage.getContent().stream()
                .map(review -> review.getProject().getId())
                .distinct()
                .toList();
        Map<Long, Proposal> acceptedProposalByProjectId = proposalRepository.findAcceptedProposalsByProjectIds(projectIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        proposal -> proposal.getProject().getId(),
                        Function.identity()
                ));

        return PageResponse.from(reviewPage.map(review -> ReviewSummaryResponse.from(
                review,
                acceptedProposalByProjectId.get(review.getProject().getId()),
                false
        )));
    }

    public List<ReviewTagCodeResponse> getActiveTagCodes() {
        return reviewTagCodeRepository.findAllByActiveYnTrueOrderBySortOrderAscCodeAsc().stream()
                .map(ReviewTagCodeResponse::from)
                .toList();
    }

    private Review getOwnedReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findDetailById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.isWrittenBy(currentUserId)) {
            throw new CustomException(ErrorCode.REVIEW_ACCESS_DENIED);
        }
        return review;
    }

    private Proposal getAcceptedProposal(Long projectId) {
        return proposalRepository.findAcceptedProposalByProjectId(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_ELIGIBLE));
    }

    private List<String> normalizeTagCodes(Collection<String> rawTagCodes) {
        if (rawTagCodes == null) {
            return List.of();
        }

        List<String> normalizedTagCodes = rawTagCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        if (normalizedTagCodes.isEmpty()) {
            return List.of();
        }

        Set<String> activeTagCodes = reviewTagCodeRepository.findAllByCodeInAndActiveYnTrue(normalizedTagCodes).stream()
                .map(reviewTagCode -> reviewTagCode.getCode())
                .collect(java.util.stream.Collectors.toSet());

        List<String> unsupportedTagCodes = normalizedTagCodes.stream()
                .filter(tagCode -> !activeTagCodes.contains(tagCode))
                .toList();

        if (!unsupportedTagCodes.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "tagCodes: unsupported code(s) " + unsupportedTagCodes);
        }

        return normalizedTagCodes;
    }
}
