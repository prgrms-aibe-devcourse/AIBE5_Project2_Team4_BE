package com.ieum.ansimdonghaeng.domain.proposal.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalDetailResponse;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalListResponse;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/freelancers/me/proposals")
@RequiredArgsConstructor
public class FreelancerProposalController {

    private final ProposalService proposalService;

    // 프리랜서는 자신에게 도착한 제안만 페이지로 확인할 수 있다.
    @Operation(summary = "프리랜서 제안 목록 조회")
    @GetMapping
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalListResponse>> getMyProposals(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ProposalStatus status,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        ProposalListResponse response = proposalService.getMyProposals(
                AuthenticatedUserSupport.currentUserId(userDetails),
                status,
                page,
                size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 프리랜서는 본인에게 연결된 제안 상세만 조회할 수 있다.
    @Operation(summary = "프리랜서 제안 상세 조회")
    @GetMapping("/{proposalId}")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalDetailResponse>> getMyProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long proposalId
    ) {
        ProposalDetailResponse response = proposalService.getMyProposal(
                AuthenticatedUserSupport.currentUserId(userDetails),
                proposalId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 제안 수락 시 프로젝트 상태까지 함께 ACCEPTED로 전이한다.
    @Operation(summary = "프리랜서 제안 수락")
    @PatchMapping("/{proposalId}/accept")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ProposalDetailResponse>> acceptProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long proposalId
    ) {
        ProposalDetailResponse response = proposalService.acceptProposal(
                AuthenticatedUserSupport.currentUserId(userDetails),
                proposalId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
