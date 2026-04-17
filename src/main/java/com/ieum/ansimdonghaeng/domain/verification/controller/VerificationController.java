package com.ieum.ansimdonghaeng.domain.verification.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationCreateRequest;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationFileResponse;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationResponse;
import com.ieum.ansimdonghaeng.domain.verification.service.VerificationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/freelancers/me/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<VerificationResponse>> createMyVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VerificationCreateRequest request
    ) {
        VerificationResponse response = verificationService.createMyVerification(
                AuthenticatedUserSupport.currentUserId(userDetails),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VerificationResponse>>> getMyVerifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.getMyVerifications(AuthenticatedUserSupport.currentUserId(userDetails))
        ));
    }

    @GetMapping("/{verificationRequestId}")
    public ResponseEntity<ApiResponse<VerificationResponse>> getMyVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationRequestId
    ) {
        VerificationResponse response = verificationService.getMyVerification(
                AuthenticatedUserSupport.currentUserId(userDetails),
                verificationRequestId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{verificationRequestId}/files")
    public ResponseEntity<ApiResponse<VerificationFileResponse>> uploadMyVerificationFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationRequestId,
            @RequestPart MultipartFile file
    ) {
        VerificationFileResponse response = verificationService.uploadMyVerificationFile(
                AuthenticatedUserSupport.currentUserId(userDetails),
                verificationRequestId,
                file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{verificationRequestId}/files")
    public ResponseEntity<ApiResponse<List<VerificationFileResponse>>> getMyVerificationFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long verificationRequestId
    ) {
        return ResponseEntity.ok(ApiResponse.success(verificationService.getMyVerificationFiles(
                AuthenticatedUserSupport.currentUserId(userDetails),
                verificationRequestId
        )));
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyVerificationFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long fileId
    ) {
        verificationService.deleteMyVerificationFile(AuthenticatedUserSupport.currentUserId(userDetails), fileId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.empty());
    }
}
