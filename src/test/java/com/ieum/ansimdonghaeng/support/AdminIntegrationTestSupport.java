package com.ieum.ansimdonghaeng.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerFileRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.notice.entity.Notice;
import com.ieum.ansimdonghaeng.domain.notice.repository.NoticeRepository;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.report.entity.Report;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportReasonType;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public abstract class AdminIntegrationTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected FreelancerProfileRepository freelancerProfileRepository;

    @Autowired
    protected FreelancerFileRepository freelancerFileRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected ProposalRepository proposalRepository;

    @Autowired
    protected ReviewRepository reviewRepository;

    @Autowired
    protected ReportRepository reportRepository;

    @Autowired
    protected VerificationRepository verificationRepository;

    @Autowired
    protected VerificationFileRepository verificationFileRepository;

    @Autowired
    protected NoticeRepository noticeRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUpAdminDataBeforeEach() {
        cleanUpAdminData();
    }

    @AfterEach
    void cleanUpAdminDataAfterEach() {
        cleanUpAdminData();
    }

    private void cleanUpAdminData() {
        notificationRepository.deleteAll();
        freelancerFileRepository.deleteAll();
        verificationFileRepository.deleteAll();
        reportRepository.deleteAll();
        reviewRepository.deleteAll();
        proposalRepository.deleteAll();
        noticeRepository.deleteAll();
        verificationRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected RequestPostProcessor adminPrincipal(User user) {
        return principal(user, UserRole.ADMIN);
    }

    protected RequestPostProcessor userPrincipal(User user) {
        return principal(user, UserRole.USER);
    }

    protected RequestPostProcessor freelancerPrincipal(User user) {
        return principal(user, UserRole.FREELANCER);
    }

    protected User saveUser(String email, String name, UserRole role) {
        return saveUser(email, name, role, true);
    }

    protected User saveUser(String email, String name, UserRole role, boolean active) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("1234"))
                .name(name)
                .phone("010-0000-0000")
                .intro("intro")
                .roleCode(role.getCode())
                .activeYn(active)
                .build());
    }

    protected FreelancerProfile saveFreelancerProfile(User user, boolean verified, boolean publicYn) {
        return freelancerProfileRepository.save(FreelancerProfile.create(
                user,
                "career",
                true,
                verified,
                new BigDecimal("4.50"),
                12L,
                publicYn,
                Set.of("SEOUL_GANGNAM"),
                Set.of("MORNING"),
                Set.of("HOSPITAL_COMPANION")
        ));
    }

    protected Project saveProject(User owner, ProjectStatus status) {
        Project project = Project.create(
                owner.getId(),
                "project-" + status.name(),
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 20, 9, 0),
                LocalDateTime.of(2026, 4, 20, 12, 0),
                "Seoul address",
                "detail address",
                "request detail"
        );
        project = projectRepository.saveAndFlush(project);
        switch (status) {
            case ACCEPTED -> project.accept(LocalDateTime.of(2026, 4, 19, 9, 0));
            case IN_PROGRESS -> {
                project.accept(LocalDateTime.of(2026, 4, 19, 9, 0));
                project.start(LocalDateTime.of(2026, 4, 20, 9, 0));
            }
            case COMPLETED -> {
                project.accept(LocalDateTime.of(2026, 4, 19, 9, 0));
                project.start(LocalDateTime.of(2026, 4, 20, 9, 0));
                project.complete(LocalDateTime.of(2026, 4, 20, 12, 0));
            }
            case CANCELLED -> project.cancel("cancelled", LocalDateTime.of(2026, 4, 18, 9, 0));
            default -> {
            }
        }
        return projectRepository.saveAndFlush(project);
    }

    protected Proposal saveAcceptedProposal(Project project, FreelancerProfile freelancerProfile) {
        Proposal proposal = Proposal.create(project, freelancerProfile, "accepted proposal");
        proposal = proposalRepository.saveAndFlush(proposal);
        proposal.accept(LocalDateTime.of(2026, 4, 19, 10, 0));
        return proposalRepository.saveAndFlush(proposal);
    }

    protected Review saveReview(Project project, int rating, boolean blinded) {
        Review review = Review.create(project, project.getOwnerUserId(), rating, "review content", java.util.List.of());
        review = reviewRepository.saveAndFlush(review);
        if (blinded) {
            review.blind();
        }
        return reviewRepository.saveAndFlush(review);
    }

    protected Report saveReport(Review review, User reporter, ReportReasonType reasonType, ReportStatus status, User handledBy) {
        Report report = Report.create(review, reporter, reasonType, "report detail");
        report = reportRepository.saveAndFlush(report);
        if (status == ReportStatus.RESOLVED && handledBy != null) {
            report.resolve(handledBy, LocalDateTime.of(2026, 4, 21, 9, 0));
        } else if (status == ReportStatus.REJECTED && handledBy != null) {
            report.reject(handledBy, LocalDateTime.of(2026, 4, 21, 9, 0));
        }
        return reportRepository.saveAndFlush(report);
    }

    protected Verification saveVerification(FreelancerProfile profile,
                                            VerificationType type,
                                            VerificationStatus status,
                                            User reviewedBy,
                                            String rejectReason) {
        Verification verification = Verification.create(
                profile,
                type,
                "verification description",
                LocalDateTime.of(2026, 4, 17, 9, 0)
        );
        verification = verificationRepository.saveAndFlush(verification);
        if (status == VerificationStatus.APPROVED && reviewedBy != null) {
            verification.approve(reviewedBy, LocalDateTime.of(2026, 4, 18, 9, 0));
            profile.updateVerifiedYn(true);
        } else if (status == VerificationStatus.REJECTED && reviewedBy != null) {
            verification.reject(reviewedBy, rejectReason == null ? "reject reason" : rejectReason, LocalDateTime.of(2026, 4, 18, 9, 0));
            profile.updateVerifiedYn(false);
        }
        freelancerProfileRepository.saveAndFlush(profile);
        return verificationRepository.saveAndFlush(verification);
    }

    protected VerificationFile saveVerificationFile(Verification verification) {
        return verificationFileRepository.saveAndFlush(VerificationFile.create(
                verification,
                "proof.pdf",
                "proof-1.pdf",
                "https://example.com/proof.pdf",
                "application/pdf",
                1024L,
                LocalDateTime.of(2026, 4, 17, 9, 30)
        ));
    }

    protected Notice saveNotice(User admin, boolean published) {
        Notice notice = Notice.create(admin, "notice title", "notice content", published, LocalDateTime.of(2026, 4, 16, 9, 0));
        return noticeRepository.saveAndFlush(notice);
    }

    protected List<Proposal> saveAcceptedProposals(Project project, FreelancerProfile... freelancerProfiles) {
        return java.util.Arrays.stream(freelancerProfiles).map(profile -> saveAcceptedProposal(project, profile)).toList();
    }

    private RequestPostProcessor principal(User user, UserRole role) {
        return user(CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .password("{noop}password")
                .authorities(List.of(new SimpleGrantedAuthority(role.asAuthority())))
                .enabled(Boolean.TRUE.equals(user.getActiveYn()))
                .build());
    }
}
