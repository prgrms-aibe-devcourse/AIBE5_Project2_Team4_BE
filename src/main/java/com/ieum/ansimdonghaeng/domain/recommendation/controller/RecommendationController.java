package com.ieum.ansimdonghaeng.domain.recommendation.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.recommendation.dto.request.FreelancerRecommendationRequest;
import com.ieum.ansimdonghaeng.domain.recommendation.dto.response.FreelancerRecommendationResponse;
import com.ieum.ansimdonghaeng.domain.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/freelancers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<FreelancerRecommendationResponse>> recommendFreelancers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FreelancerRecommendationRequest request
    ) {
        FreelancerRecommendationResponse response = recommendationService.recommendFreelancers(
                AuthenticatedUserSupport.currentUserId(userDetails),
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/freelancers/public")
    public ResponseEntity<ApiResponse<FreelancerRecommendationResponse>> recommendFreelancersPublic(
            @Valid @RequestBody FreelancerRecommendationRequest request
    ) {
        FreelancerRecommendationResponse response = recommendationService.recommendFreelancers(null, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
