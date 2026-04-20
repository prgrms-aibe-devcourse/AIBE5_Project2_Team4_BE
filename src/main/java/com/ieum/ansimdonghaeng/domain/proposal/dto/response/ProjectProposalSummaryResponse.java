package com.ieum.ansimdonghaeng.domain.proposal.dto.response;

import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProjectProposalSummaryView;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectProposalSummaryResponse(
        Long proposalId,
        Long projectId,
        Long freelancerProfileId,
        FreelancerSummary freelancer,
        ProposalStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {

    public static ProjectProposalSummaryResponse from(ProjectProposalSummaryView proposal) {
        return new ProjectProposalSummaryResponse(
                proposal.proposalId(),
                proposal.projectId(),
                proposal.freelancerProfileId(),
                new FreelancerSummary(
                        proposal.freelancerUserId(),
                        proposal.freelancerName(),
                        proposal.freelancerVerifiedYn(),
                        proposal.freelancerAverageRating()
                ),
                proposal.status(),
                proposal.message(),
                proposal.createdAt(),
                proposal.respondedAt()
        );
    }

    public record FreelancerSummary(
            Long userId,
            String name,
            Boolean verifiedYn,
            BigDecimal rating
    ) {
    }
}
