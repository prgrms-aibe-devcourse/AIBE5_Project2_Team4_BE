package com.ieum.ansimdonghaeng.domain.report.service;

import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.report.dto.request.ReportCreateRequest;
import com.ieum.ansimdonghaeng.domain.report.dto.response.ReportCreateResponse;
import com.ieum.ansimdonghaeng.domain.report.dto.response.ReportSummaryResponse;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    ReportCreateResponse createReport(Long currentUserId, Long reviewId, ReportCreateRequest request);

    PageResponse<ReportSummaryResponse> getMyReports(Long currentUserId, ReportStatus status, Pageable pageable);
}
