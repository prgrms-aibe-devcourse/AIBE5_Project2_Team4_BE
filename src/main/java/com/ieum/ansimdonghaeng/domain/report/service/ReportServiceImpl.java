package com.ieum.ansimdonghaeng.domain.report.service;

import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.report.dto.request.ReportCreateRequest;
import com.ieum.ansimdonghaeng.domain.report.dto.response.ReportCreateResponse;
import com.ieum.ansimdonghaeng.domain.report.dto.response.ReportSummaryResponse;
import com.ieum.ansimdonghaeng.domain.report.entity.Report;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReportCreateResponse createReport(Long currentUserId, Long reviewId, ReportCreateRequest request) {
        User reporter = getActiveUser(currentUserId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        if (reportRepository.existsByReview_IdAndReporterUser_Id(reviewId, currentUserId)) {
            throw new CustomException(ErrorCode.REPORT_DUPLICATE);
        }

        Report report = reportRepository.save(Report.create(review, reporter, request.reasonType(), request.reasonDetail()));
        return ReportCreateResponse.from(report);
    }

    @Override
    public PageResponse<ReportSummaryResponse> getMyReports(Long currentUserId, ReportStatus status, Pageable pageable) {
        getActiveUser(currentUserId);
        Page<Report> reportPage = status == null
                ? reportRepository.findAllByReporterUser_IdOrderByCreatedAtDescIdDesc(currentUserId, pageable)
                : reportRepository.findAllByReporterUser_IdAndStatusOrderByCreatedAtDescIdDesc(currentUserId, status, pageable);
        return PageResponse.from(reportPage.map(ReportSummaryResponse::from));
    }

    private User getActiveUser(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));
        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }
        return user;
    }
}
