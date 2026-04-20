package com.ieum.ansimdonghaeng.domain.proposal.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalDetailResponse;
import com.ieum.ansimdonghaeng.domain.proposal.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/proposals")
public class ProposalDecisionController {

    private final ProposalService proposalService;

    @PatchMapping("/{proposalId}/reject")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalDetailResponse>> rejectProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long proposalId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                proposalService.rejectProposal(AuthenticatedUserSupport.currentUserId(userDetails), proposalId)
        ));
    }
}
