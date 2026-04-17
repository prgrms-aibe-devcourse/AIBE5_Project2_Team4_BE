package com.ieum.ansimdonghaeng.domain.freelancer.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.request.FreelancerProfileUpsertRequest;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.FreelancerDetailResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/freelancers/me/profile")
@RequiredArgsConstructor
public class FreelancerProfileController {

    private final FreelancerService freelancerService;

    @PostMapping
    public ResponseEntity<ApiResponse<FreelancerDetailResponse>> createMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FreelancerProfileUpsertRequest request
    ) {
        FreelancerDetailResponse response = freelancerService.createMyProfile(
                AuthenticatedUserSupport.currentUserId(userDetails),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FreelancerDetailResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                freelancerService.getMyProfile(AuthenticatedUserSupport.currentUserId(userDetails))
        ));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<FreelancerDetailResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FreelancerProfileUpsertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                freelancerService.updateMyProfile(AuthenticatedUserSupport.currentUserId(userDetails), request)
        ));
    }
}
