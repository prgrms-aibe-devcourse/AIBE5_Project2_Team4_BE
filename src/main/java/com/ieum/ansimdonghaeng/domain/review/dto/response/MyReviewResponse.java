package com.ieum.ansimdonghaeng.domain.review.dto.response;

import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import java.time.LocalDateTime;

public record MyReviewResponse(
        Long reviewId,
        Long projectId,
        String projectTitle,
        Long freelancerProfileId,
        String freelancerName,
        Integer rating,
        String content,
        boolean blinded,
        boolean reported,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MyReviewResponse from(Review review, Proposal acceptedProposal, boolean reported) {
        return new MyReviewResponse(
                review.getId(),
                review.getProject().getId(),
                review.getProject().getTitle(),
                acceptedProposal == null ? null : acceptedProposal.getFreelancerProfile().getId(),
                acceptedProposal == null ? null : acceptedProposal.getFreelancerProfile().getUser().getName(),
                review.getRating(),
                review.getContent(),
                review.isBlinded(),
                reported,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
