package com.ieum.ansimdonghaeng.domain.proposal.repository;

import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProposalQueryRepository {

    Page<ProposalSummaryView> findFreelancerProposals(Long freelancerProfileId, ProposalStatus status, Pageable pageable);

    Page<ProjectProposalSummaryView> findProjectOwnerProposals(Long projectId, ProposalStatus status, Pageable pageable);
}
