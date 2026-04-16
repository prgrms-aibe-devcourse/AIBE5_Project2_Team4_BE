package com.ieum.ansimdonghaeng.domain.freelancer.dto.response;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record PublicFreelancerDetailResponse(
        Long freelancerProfileId,
        String name,
        String intro,
        String careerDescription,
        Boolean caregiverYn,
        Boolean verifiedYn,
        BigDecimal averageRating,
        Long activityCount,
        List<String> activityRegionCodes,
        List<String> availableTimeSlotCodes,
        List<String> projectTypeCodes
) {

    public static PublicFreelancerDetailResponse from(FreelancerProfile profile) {
        return new PublicFreelancerDetailResponse(
                profile.getId(),
                profile.getUser().getName(),
                profile.getUser().getIntro(),
                profile.getCareerDescription(),
                profile.getCaregiverYn(),
                profile.getVerifiedYn(),
                profile.getAverageRating(),
                profile.getActivityCount(),
                profile.getActivityRegionCodes().stream().sorted(Comparator.naturalOrder()).toList(),
                profile.getAvailableTimeSlotCodes().stream().sorted(Comparator.naturalOrder()).toList(),
                profile.getProjectTypeCodes().stream().sorted(Comparator.naturalOrder()).toList()
        );
    }
}
