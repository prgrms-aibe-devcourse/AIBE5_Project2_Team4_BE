package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReportDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReportListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminUserSummaryResponse;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminPageQuerySupport;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminResponseMapper;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.report.entity.Report;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public PageResponse<AdminReportListItemResponse> getReports(ReportStatus status,
                                                                ReportReasonType reasonType,
                                                                Pageable pageable) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder selectBuilder = new StringBuilder("""
                select new com.ieum.ansimdonghaeng.domain.admin.service.AdminReportService$ReportListRow(
                    report.id,
                    review.id,
                    reporter.id,
                    reporter.name,
                    reporter.email,
                    reporter.roleCode,
                    reporter.activeYn,
                    report.reasonType,
                    report.status,
                    report.createdAt,
                    report.handledAt,
                    handledBy.id,
                    handledBy.name,
                    handledBy.email,
                    handledBy.roleCode,
                    handledBy.activeYn
                )
                from Report report
                join report.review review
                join report.reporterUser reporter
                left join report.handledByUser handledBy
                where 1 = 1
                """);
        StringBuilder countBuilder = new StringBuilder("""
                select count(report.id)
                from Report report
                join report.review review
                join report.reporterUser reporter
                left join report.handledByUser handledBy
                where 1 = 1
                """);

        if (status != null) {
            appendCondition(selectBuilder, countBuilder, " and report.status = :status");
            parameters.put("status", status);
        }
        if (reasonType != null) {
            appendCondition(selectBuilder, countBuilder, " and report.reasonType = :reasonType");
            parameters.put("reasonType", reasonType);
        }

        selectBuilder.append(" order by ")
                .append(AdminPageQuerySupport.orderByClause(
                        pageable,
                        Map.of(
                                "reportId", "report.id",
                                "createdAt", "report.createdAt",
                                "handledAt", "report.handledAt",
                                "status", "report.status"
                        ),
                        "report.createdAt desc, report.id desc"
                ));

        TypedQuery<ReportListRow> query = entityManager.createQuery(selectBuilder.toString(), ReportListRow.class);
        bindParameters(query, parameters);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AdminReportListItemResponse> content = query.getResultList().stream()
                .map(row -> new AdminReportListItemResponse(
                        row.reportId(),
                        row.reviewId(),
                        row.reporter(),
                        row.reasonType(),
                        row.status(),
                        row.createdAt(),
                        row.handledAt(),
                        row.handledBy()
                ))
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countBuilder.toString(), Long.class);
        bindParameters(countQuery, parameters);
        long totalElements = countQuery.getSingleResult();

        return AdminPageQuerySupport.toPageResponse(content, pageable, totalElements);
    }

    public AdminReportDetailResponse getReport(Long reportId) {
        Report report = reportRepository.findDetailById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        return toDetailResponse(report);
    }

    @Transactional
    public AdminReportDetailResponse resolve(Long adminUserId, Long reportId) {
        Report report = reportRepository.findDetailByIdForUpdate(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        if (report.isHandled()) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_HANDLED);
        }
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Admin user was not found."));
        report.resolve(adminUser, java.time.LocalDateTime.now());
        return toDetailResponse(report);
    }

    @Transactional
    public AdminReportDetailResponse reject(Long adminUserId, Long reportId) {
        Report report = reportRepository.findDetailByIdForUpdate(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        if (report.isHandled()) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_HANDLED);
        }
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Admin user was not found."));
        report.reject(adminUser, java.time.LocalDateTime.now());
        return toDetailResponse(report);
    }

    private AdminReportDetailResponse toDetailResponse(Report report) {
        Review review = report.getReview();
        Project project = review.getProject();
        Proposal acceptedProposal = proposalRepository.findAcceptedProposalByProjectId(project.getId()).orElse(null);

        return new AdminReportDetailResponse(
                report.getId(),
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getHandledAt(),
                AdminResponseMapper.toUserSummary(report.getReporterUser()),
                AdminResponseMapper.toUserSummary(report.getHandledByUser()),
                new AdminReportDetailResponse.ReviewSummaryResponse(
                        review.getId(),
                        project.getId(),
                        project.getTitle(),
                        review.getRating(),
                        review.isBlinded(),
                        AdminResponseMapper.toUserSummary(project.getOwnerUser()),
                        acceptedProposal != null ? AdminResponseMapper.toFreelancerSummary(acceptedProposal.getFreelancerProfile()) : null,
                        review.getCreatedAt()
                )
        );
    }

    private void appendCondition(StringBuilder selectBuilder, StringBuilder countBuilder, String condition) {
        selectBuilder.append(condition);
        countBuilder.append(condition);
    }

    private void bindParameters(jakarta.persistence.Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private record ReportListRow(
            Long reportId,
            Long reviewId,
            Long reporterUserId,
            String reporterName,
            String reporterEmail,
            String reporterRoleCode,
            Boolean reporterActiveYn,
            ReportReasonType reasonType,
            ReportStatus status,
            LocalDateTime createdAt,
            LocalDateTime handledAt,
            Long handledByUserId,
            String handledByName,
            String handledByEmail,
            String handledByRoleCode,
            Boolean handledByActiveYn
    ) {
        private AdminUserSummaryResponse reporter() {
            return new AdminUserSummaryResponse(
                    reporterUserId,
                    reporterName,
                    reporterEmail,
                    reporterRoleCode,
                    reporterActiveYn
            );
        }

        private AdminUserSummaryResponse handledBy() {
            if (handledByUserId == null) {
                return null;
            }
            return new AdminUserSummaryResponse(
                    handledByUserId,
                    handledByName,
                    handledByEmail,
                    handledByRoleCode,
                    handledByActiveYn
            );
        }
    }
}
