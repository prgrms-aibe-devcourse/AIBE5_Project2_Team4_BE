package com.ieum.ansimdonghaeng.domain.freelancer.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.FreelancerListResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.PublicFreelancerDetailResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/freelancers")
@RequiredArgsConstructor
public class FreelancerController {

    private final FreelancerService freelancerService;

    // 공개 프리랜서 목록을 페이지 단위로 내려준다.
    @Operation(summary = "프리랜서 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<FreelancerListResponse>> getFreelancers(
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(freelancerService.getFreelancers(page, size)));
    }

    // 공개 가능한 프리랜서 상세 정보만 조회할 수 있다.
    @Operation(summary = "프리랜서 상세 조회")
    @GetMapping("/{freelancerProfileId}")
    public ResponseEntity<ApiResponse<PublicFreelancerDetailResponse>> getFreelancer(
            @PathVariable Long freelancerProfileId
    ) {
        return ResponseEntity.ok(ApiResponse.success(freelancerService.getFreelancer(freelancerProfileId)));
    }
}
