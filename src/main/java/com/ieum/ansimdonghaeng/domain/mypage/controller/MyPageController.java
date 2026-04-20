package com.ieum.ansimdonghaeng.domain.mypage.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.mypage.dto.response.MyPageSummaryResponse;
import com.ieum.ansimdonghaeng.domain.mypage.service.MyPageService;
import com.ieum.ansimdonghaeng.domain.review.dto.response.MyReviewListResponse;
import com.ieum.ansimdonghaeng.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final ReviewService reviewService;

    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MyPageSummaryResponse>> getMyPageSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                myPageService.getMyPageSummary(AuthenticatedUserSupport.currentUserId(userDetails))
        ));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<MyReviewListResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getMyReviews(AuthenticatedUserSupport.currentUserId(userDetails), pageable)
        ));
    }
}
