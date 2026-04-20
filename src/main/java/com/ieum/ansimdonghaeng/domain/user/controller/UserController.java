package com.ieum.ansimdonghaeng.domain.user.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.user.dto.request.UserProfileUpdateRequest;
import com.ieum.ansimdonghaeng.domain.user.dto.response.UserMyPageResponse;
import com.ieum.ansimdonghaeng.domain.user.dto.response.PublicUserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.dto.response.UserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(userDetails.getUserId())));
    }

    @GetMapping("/me/mypage")
    public ResponseEntity<ApiResponse<UserMyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyPage(userDetails.getUserId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
                                                                            @AuthenticationPrincipal CustomUserDetails userDetails,
                                                                            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateMyProfile(userDetails.getUserId(), request)));
    }

    @GetMapping("/{userId}/public-profile")
    public ResponseEntity<ApiResponse<PublicUserProfileResponse>> getPublicProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPublicProfile(userId)));
    }
}
