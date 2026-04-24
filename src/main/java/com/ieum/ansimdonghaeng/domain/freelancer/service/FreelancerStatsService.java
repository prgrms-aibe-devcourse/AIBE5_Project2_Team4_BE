package com.ieum.ansimdonghaeng.domain.freelancer.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FreelancerStatsService {

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ProposalRepository proposalRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void refreshStats(Long freelancerProfileId) {
        FreelancerProfile profile = freelancerProfileRepository.findById(freelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));

        long activityCount = proposalRepository.countCompletedAcceptedProjectsByFreelancerProfileId(freelancerProfileId);
        Double averageRatingValue = reviewRepository.findPublicAverageRatingByFreelancerProfileId(freelancerProfileId);
        BigDecimal averageRating = averageRatingValue == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(averageRatingValue).setScale(2, RoundingMode.HALF_UP);

        profile.updateStats(averageRating, activityCount);
    }
}
