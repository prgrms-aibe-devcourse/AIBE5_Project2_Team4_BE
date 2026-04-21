package com.ieum.ansimdonghaeng.domain.code.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.domain.code.dto.response.CodeLookupResponse;
import com.ieum.ansimdonghaeng.domain.code.service.CodeLookupService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeLookupService codeLookupService;

    @GetMapping("/project-types")
    public ResponseEntity<ApiResponse<List<CodeLookupResponse>>> getProjectTypes() {
        return ResponseEntity.ok(ApiResponse.success(codeLookupService.getProjectTypes()));
    }

    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<List<CodeLookupResponse>>> getRegions() {
        return ResponseEntity.ok(ApiResponse.success(codeLookupService.getRegions()));
    }

    @GetMapping("/available-time-slots")
    public ResponseEntity<ApiResponse<List<CodeLookupResponse>>> getAvailableTimeSlots() {
        return ResponseEntity.ok(ApiResponse.success(codeLookupService.getAvailableTimeSlots()));
    }
}
