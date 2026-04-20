package com.ieum.ansimdonghaeng.domain.user.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.user.dto.request.UserProfileUpdateRequest;
import com.ieum.ansimdonghaeng.domain.user.dto.response.PublicUserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.dto.response.UserMyPageResponse;
import com.ieum.ansimdonghaeng.domain.user.dto.response.UserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ProposalRepository proposalRepository;
    private final VerificationRepository verificationRepository;

    public UserProfileResponse getMyProfile(Long userId) {
        return toProfileResponse(getActiveUserById(userId));
    }

    public UserMyPageResponse getMyPage(Long userId) {
        User user = getActiveUserById(userId);

        UserMyPageResponse.ProjectStats projectStats = new UserMyPageResponse.ProjectStats(
                projectRepository.countByOwnerUserId(userId),
                projectRepository.countByOwnerUserIdAndStatus(userId, ProjectStatus.REQUESTED),
                projectRepository.countByOwnerUserIdAndStatus(userId, ProjectStatus.ACCEPTED),
                projectRepository.countByOwnerUserIdAndStatus(userId, ProjectStatus.IN_PROGRESS),
                projectRepository.countByOwnerUserIdAndStatus(userId, ProjectStatus.COMPLETED),
                projectRepository.countByOwnerUserIdAndStatus(userId, ProjectStatus.CANCELLED)
        );

        UserMyPageResponse.ReviewStats reviewStats = new UserMyPageResponse.ReviewStats(
                reviewRepository.countByReviewerUserId(userId),
                reportRepository.countByReporterUser_Id(userId)
        );

        UserMyPageResponse.NotificationSummary notificationSummary = new UserMyPageResponse.NotificationSummary(
                notificationRepository.countByUser_IdAndReadYnFalse(userId)
        );

        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId).orElse(null);
        UserMyPageResponse.FreelancerProfileSummary freelancerProfileSummary = null;
        UserMyPageResponse.VerificationSummary verificationSummary = null;
        UserMyPageResponse.ProposalSummary proposalSummary = null;

        if (profile != null) {
            freelancerProfileSummary = new UserMyPageResponse.FreelancerProfileSummary(
                    profile.getId(),
                    profile.getVerifiedYn(),
                    profile.getPublicYn(),
                    profile.getCaregiverYn(),
                    profile.getAverageRating(),
                    profile.getActivityCount()
            );

            Verification latestVerification = verificationRepository
                    .findTopByFreelancerProfile_IdOrderByRequestedAtDescIdDesc(profile.getId())
                    .orElse(null);
            if (latestVerification != null) {
                verificationSummary = new UserMyPageResponse.VerificationSummary(
                        latestVerification.getId(),
                        latestVerification.getVerificationType(),
                        latestVerification.getStatus(),
                        latestVerification.getRequestedAt(),
                        latestVerification.getReviewedAt(),
                        latestVerification.getRejectReason()
                );
            }

            proposalSummary = new UserMyPageResponse.ProposalSummary(
                    proposalRepository.countByFreelancerProfile_Id(profile.getId()),
                    proposalRepository.countByFreelancerProfile_IdAndStatus(profile.getId(), ProposalStatus.PENDING)
            );
        }

        return new UserMyPageResponse(
                new UserMyPageResponse.UserSummary(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getPhone(),
                        user.getIntro(),
                        user.getRoleCode(),
                        Boolean.TRUE.equals(user.getActiveYn())
                ),
                projectStats,
                reviewStats,
                notificationSummary,
                freelancerProfileSummary,
                verificationSummary,
                proposalSummary
        );
    }

    @Transactional
    public UserProfileResponse updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getActiveUserById(userId);
        user.updateProfile(request.name(), request.phone(), request.intro());
        return toProfileResponse(user);
    }

    public PublicUserProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .filter(savedUser -> Boolean.TRUE.equals(savedUser.getActiveYn()))
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));

        return new PublicUserProfileResponse(
                user.getId(),
                user.getName(),
                user.getIntro(),
                user.getRoleCode()
        );
    }

    private User getActiveUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));

        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }

        return user;
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getIntro(),
                user.getRoleCode(),
                Boolean.TRUE.equals(user.getActiveYn())
        );
    }
}
