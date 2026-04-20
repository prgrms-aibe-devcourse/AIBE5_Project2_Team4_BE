package com.ieum.ansimdonghaeng.domain.review.dto.response;

import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ReviewDetailResponse(
        Long reviewId,
        Long projectId,
        String projectTitle,
        Long freelancerProfileId,
        String freelancerName,
        Long reviewerUserId,
        String reviewerName,
        Integer rating,
        List<String> tagCodes,
        String content,
        Boolean blindedYn,
        Boolean reported,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ReviewDetailResponse from(Review review, Proposal acceptedProposal, boolean reported) {
        return new ReviewDetailResponse(
                review.getId(),
                review.getProject().getId(),
                review.getProject().getTitle(),
                acceptedProposal == null ? null : acceptedProposal.getFreelancerProfile().getId(),
                acceptedProposal == null ? null : acceptedProposal.getFreelancerProfile().getUser().getName(),
                review.getReviewerUserId(),
                review.getReviewerUser() != null ? review.getReviewerUser().getName() : null,
                review.getRating(),
                review.getTags().stream()
                        .map(tag -> tag.getTagCode())
                        .sorted(Comparator.naturalOrder())
                        .toList(),
                review.getContent(),
                review.isBlinded(),
                reported,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
