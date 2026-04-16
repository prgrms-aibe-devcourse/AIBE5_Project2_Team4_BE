package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminDashboardResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.notice.repository.NoticeRepository;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final VerificationRepository verificationRepository;
    private final ProjectRepository projectRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final NoticeRepository noticeRepository;

    public AdminDashboardResponse getDashboard() {
        List<AdminDashboardResponse.RecentVerification> recentVerifications =
                verificationRepository.findTop5ByStatusOrderByRequestedAtDescIdDesc(VerificationStatus.PENDING)
                        .stream()
                        .map(verification -> new AdminDashboardResponse.RecentVerification(
                                verification.getId(),
                                verification.getFreelancerProfile().getId(),
                                verification.getFreelancerProfile().getUser().getName(),
                                verification.getVerificationType().name(),
                                verification.getRequestedAt()
                        ))
                        .toList();

        List<AdminDashboardResponse.RecentReport> recentReports =
                reportRepository.findTop5ByStatusOrderByCreatedAtDescIdDesc(ReportStatus.PENDING)
                        .stream()
                        .map(report -> new AdminDashboardResponse.RecentReport(
                                report.getId(),
                                report.getReview().getId(),
                                report.getReporterUser().getName(),
                                report.getReasonType(),
                                report.getCreatedAt()
                        ))
                        .toList();

        List<AdminDashboardResponse.RecentProject> recentProjects =
                projectRepository.findTop5ByOrderByCreatedAtDescIdDesc()
                        .stream()
                        .map(project -> new AdminDashboardResponse.RecentProject(
                                project.getId(),
                                project.getTitle(),
                                project.getStatus(),
                                project.getOwnerUser() != null ? project.getOwnerUser().getName() : null,
                                project.getCreatedAt()
                        ))
                        .toList();

        return new AdminDashboardResponse(
                userRepository.count(),
                freelancerProfileRepository.count(),
                freelancerProfileRepository.countByVerifiedYnTrue(),
                verificationRepository.countByStatus(VerificationStatus.PENDING),
                projectRepository.countByStatus(ProjectStatus.REQUESTED),
                projectRepository.countByStatus(ProjectStatus.ACCEPTED),
                projectRepository.countByStatus(ProjectStatus.IN_PROGRESS),
                projectRepository.countByStatus(ProjectStatus.COMPLETED),
                projectRepository.countByStatus(ProjectStatus.CANCELLED),
                reportRepository.countByStatus(ReportStatus.PENDING),
                reviewRepository.countByBlindedYn("Y"),
                noticeRepository.countByPublishedYnTrue(),
                recentVerifications,
                recentReports,
                recentProjects
        );
    }
}
