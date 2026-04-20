package com.ieum.ansimdonghaeng.domain.proposal.repository;

import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProjectProposalSummaryView(
        Long proposalId,
        Long projectId,
        Long freelancerProfileId,
        Long freelancerUserId,
        String freelancerName,
        Boolean freelancerVerifiedYn,
        BigDecimal freelancerAverageRating,
        ProposalStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {
}
