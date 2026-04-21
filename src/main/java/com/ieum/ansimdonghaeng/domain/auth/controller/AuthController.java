package com.ieum.ansimdonghaeng.domain.auth.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthRefreshRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.AuthSignupRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ForgotPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.ResetPasswordRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthLogoutResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthSignupResponse;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.AuthTokenResponse;
import com.ieum.ansimdonghaeng.domain.auth.service.AuthService;
import com.ieum.ansimdonghaeng.domain.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.issueToken(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(@Valid @RequestBody AuthRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @Hidden
    @Deprecated
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> reissue(@Valid @RequestBody AuthRefreshRequest request) {
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header(HttpHeaders.LINK, "</api/v1/auth/refresh>; rel=\"successor-version\"")
                .body(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthSignupResponse>> signup(@Valid @RequestBody AuthSignupRequest request) {
        AuthSignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/v1/users/" + response.userId()))
                .body(ApiResponse.success(response));
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> kakaoLogin(@Valid @RequestBody KakaoOAuthLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.kakaoLogin(request)));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthLogoutResponse>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.logout(userDetails.getUsername())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetEmail(request);
        return ResponseEntity.ok(ApiResponse.empty());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}
