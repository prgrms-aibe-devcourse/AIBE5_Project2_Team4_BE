package com.ieum.ansimdonghaeng.domain.proposal.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.proposal.dto.request.ProposalCreateRequest;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProjectProposalSummaryResponse;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalCreateResponse;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/projects/{projectId}/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PageResponse<ProjectProposalSummaryResponse>>> getProjectProposals(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @RequestParam(required = false) ProposalStatus status,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                proposalService.getProjectProposals(
                        AuthenticatedUserSupport.currentUserId(userDetails),
                        projectId,
                        status,
                        page,
                        size
                )
        ));
    }

    @Operation(summary = "Create proposal")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProposalCreateResponse>> createProposal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ProposalCreateRequest request
    ) {
        ProposalCreateResponse response = proposalService.createProposal(
                AuthenticatedUserSupport.currentUserId(userDetails),
                projectId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
