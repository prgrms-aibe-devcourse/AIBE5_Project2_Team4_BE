package com.ieum.ansimdonghaeng.domain.freelancer.dto.response;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record PublicFreelancerSummaryResponse(
        Long freelancerProfileId,
        String name,
        String intro,
        Boolean caregiverYn,
        Boolean verifiedYn,
        BigDecimal averageRating,
        Long activityCount,
        List<String> activityRegionCodes,
        List<String> projectTypeCodes
) {

    public static PublicFreelancerSummaryResponse from(FreelancerProfile profile) {
        return new PublicFreelancerSummaryResponse(
                profile.getId(),
                profile.getUser().getName(),
                profile.getUser().getIntro(),
                profile.getCaregiverYn(),
                profile.getVerifiedYn(),
                profile.getAverageRating(),
                profile.getActivityCount(),
                profile.getActivityRegionCodes().stream().sorted(Comparator.naturalOrder()).toList(),
                profile.getProjectTypeCodes().stream().sorted(Comparator.naturalOrder()).toList()
        );
    }
}
