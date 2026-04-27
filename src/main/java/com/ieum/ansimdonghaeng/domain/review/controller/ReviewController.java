package com.ieum.ansimdonghaeng.domain.review.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewCreateRequest;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewUpdateRequest;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewDeleteResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewDetailResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewSummaryResponse;
import com.ieum.ansimdonghaeng.domain.review.dto.response.ReviewTagCodeResponse;
import com.ieum.ansimdonghaeng.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/projects/{projectId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailResponse review = reviewService.createReview(
                AuthenticatedUserSupport.currentUserId(userDetails),
                projectId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(review));
    }

    @PostMapping("/projects/{projectId}/requester-reviews")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> createRequesterReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailResponse review = reviewService.createRequesterReview(
                AuthenticatedUserSupport.currentUserId(userDetails),
                projectId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(review));
    }

    @GetMapping("/users/me/reviews")
    @PreAuthorize("hasAnyRole('USER','FREELANCER')")
    public ResponseEntity<ApiResponse<PageResponse<ReviewSummaryResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getMyReviews(
                        AuthenticatedUserSupport.currentUserId(userDetails),
                        PageRequest.of(page, size)
                )
        ));
    }

    @GetMapping("/users/me/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('USER','FREELANCER')")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getMyReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getMyReview(AuthenticatedUserSupport.currentUserId(userDetails), reviewId)
        ));
    }

    @GetMapping("/users/me/received-reviews")
    @PreAuthorize("hasAnyRole('USER','FREELANCER')")
    public ResponseEntity<ApiResponse<PageResponse<ReviewSummaryResponse>>> getMyReceivedReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getMyReceivedReviews(
                        AuthenticatedUserSupport.currentUserId(userDetails),
                        PageRequest.of(page, size)
                )
        ));
    }

    @PatchMapping("/users/me/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('USER','FREELANCER')")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> updateMyReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.updateMyReview(AuthenticatedUserSupport.currentUserId(userDetails), reviewId, request)
        ));
    }

    @DeleteMapping("/users/me/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('USER','FREELANCER')")
    public ResponseEntity<ApiResponse<ReviewDeleteResponse>> deleteMyReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.deleteMyReview(AuthenticatedUserSupport.currentUserId(userDetails), reviewId)
        ));
    }

    @GetMapping("/freelancers/{freelancerProfileId}/reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewSummaryResponse>>> getPublicFreelancerReviews(
            @PathVariable Long freelancerProfileId,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getPublicFreelancerReviews(freelancerProfileId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/reviews/tag-codes")
    public ResponseEntity<ApiResponse<List<ReviewTagCodeResponse>>> getReviewTagCodes() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getActiveTagCodes()));
    }
}
