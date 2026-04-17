package com.ieum.ansimdonghaeng.domain.freelancer.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.FreelancerFileResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/v1/freelancers/me/files")
@RequiredArgsConstructor
public class FreelancerFileController {

    private final FreelancerService freelancerService;

    @PostMapping
    public ResponseEntity<ApiResponse<FreelancerFileResponse>> uploadMyFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart MultipartFile file
    ) {
        FreelancerFileResponse response = freelancerService.uploadMyFile(
                AuthenticatedUserSupport.currentUserId(userDetails),
                file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FreelancerFileResponse>>> getMyFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                freelancerService.getMyFiles(AuthenticatedUserSupport.currentUserId(userDetails))
        ));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long fileId
    ) {
        freelancerService.deleteMyFile(AuthenticatedUserSupport.currentUserId(userDetails), fileId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.empty());
    }
}
