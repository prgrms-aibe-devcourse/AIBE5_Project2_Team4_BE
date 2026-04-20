package com.ieum.ansimdonghaeng.domain.mypage.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerFileRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.mypage.dto.response.MyPageSummaryResponse;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.user.dto.response.UserProfileResponse;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final FreelancerFileRepository freelancerFileRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final VerificationRequestRepository verificationRequestRepository;

    public MyPageSummaryResponse getMyPageSummary(Long currentUserId) {
        User user = getActiveUser(currentUserId);

        return new MyPageSummaryResponse(
                new UserProfileResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getPhone(),
                        user.getIntro(),
                        user.getRoleCode(),
                        Boolean.TRUE.equals(user.getActiveYn())
                ),
                new MyPageSummaryResponse.ProjectSummary(
                        projectRepository.countByOwnerUserId(currentUserId),
                        projectRepository.countByOwnerUserIdAndStatus(currentUserId, ProjectStatus.REQUESTED),
                        projectRepository.countByOwnerUserIdAndStatus(currentUserId, ProjectStatus.ACCEPTED),
                        projectRepository.countByOwnerUserIdAndStatus(currentUserId, ProjectStatus.IN_PROGRESS),
                        projectRepository.countByOwnerUserIdAndStatus(currentUserId, ProjectStatus.COMPLETED),
                        projectRepository.countByOwnerUserIdAndStatus(currentUserId, ProjectStatus.CANCELLED)
                ),
                new MyPageSummaryResponse.ReviewSummary(
                        reviewRepository.countByReviewerUser_Id(currentUserId)
                ),
                new MyPageSummaryResponse.NotificationSummary(
                        notificationRepository.countByUser_IdAndReadYnFalse(currentUserId)
                ),
                freelancerProfileRepository.findByUserId(currentUserId)
                        .map(this::toFreelancerSummary)
                        .orElse(null)
        );
    }

    private MyPageSummaryResponse.FreelancerSummary toFreelancerSummary(FreelancerProfile profile) {
        return new MyPageSummaryResponse.FreelancerSummary(
                profile.getId(),
                profile.getVerifiedYn(),
                profile.getPublicYn(),
                profile.getAverageRating(),
                profile.getActivityCount(),
                proposalRepository.countByFreelancerProfile_Id(profile.getId()),
                proposalRepository.countByFreelancerProfile_IdAndStatus(profile.getId(), ProposalStatus.PENDING),
                verificationRequestRepository.countByFreelancerProfile_User_Id(profile.getUser().getId()),
                verificationRequestRepository.countByFreelancerProfile_User_IdAndStatus(
                        profile.getUser().getId(),
                        VerificationStatus.PENDING
                ),
                freelancerFileRepository.countByFreelancerProfile_Id(profile.getId())
        );
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
