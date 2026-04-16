package com.ieum.ansimdonghaeng.domain.notice.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.notice.dto.response.NoticeDetailResponse;
import com.ieum.ansimdonghaeng.domain.notice.dto.response.NoticeSummaryResponse;
import com.ieum.ansimdonghaeng.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NoticeSummaryResponse>>> getNotices(
            @PageableDefault(sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotices(pageable)));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotice(noticeId)));
    }
}
