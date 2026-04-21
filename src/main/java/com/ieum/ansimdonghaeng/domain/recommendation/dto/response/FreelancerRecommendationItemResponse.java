package com.ieum.ansimdonghaeng.domain.recommendation.dto.response;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record FreelancerRecommendationItemResponse(
        Integer rank,
        Long freelancerProfileId,
        Long userId,
        String name,
        String intro,
        String careerDescription,
        Boolean caregiverYn,
        Boolean verifiedYn,
        BigDecimal averageRating,
        Long activityCount,
        List<String> activityRegionCodes,
        List<String> availableTimeSlotCodes,
        List<String> projectTypeCodes,
        Integer matchScore,
        Integer reHireRate,
        List<String> matchReasons
) {

    public static FreelancerRecommendationItemResponse from(
            int rank,
            FreelancerProfile profile,
            int matchScore,
            int reHireRate,
            List<String> matchReasons
    ) {
        return new FreelancerRecommendationItemResponse(
                rank,
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getIntro(),
                profile.getCareerDescription(),
                profile.getCaregiverYn(),
                profile.getVerifiedYn(),
                profile.getAverageRating(),
                profile.getActivityCount(),
                profile.getActivityRegionCodes().stream().sorted(Comparator.naturalOrder()).toList(),
                profile.getAvailableTimeSlotCodes().stream().sorted(Comparator.naturalOrder()).toList(),
                profile.getProjectTypeCodes().stream().sorted(Comparator.naturalOrder()).toList(),
                matchScore,
                reHireRate,
                matchReasons
        );
    }
}
