package com.ieum.ansimdonghaeng.domain.project.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @GetMapping("/bootstrap")
    public ResponseEntity<ApiResponse<Map<String, String>>> bootstrap() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "domain", "project",
                "status", "ready"
        )));
    }
}
