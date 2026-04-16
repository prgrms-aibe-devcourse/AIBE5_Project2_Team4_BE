package com.ieum.ansimdonghaeng.domain.freelancer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record FreelancerProfileUpsertRequest(
        @Size(max = 1000, message = "careerDescription must be 1000 characters or fewer")
        String careerDescription,

        @NotNull(message = "caregiverYn is required")
        Boolean caregiverYn,

        @NotNull(message = "publicYn is required")
        Boolean publicYn,

        @Size(max = 20, message = "activityRegionCodes must contain 20 items or fewer")
        Set<@Size(max = 20, message = "activityRegionCode must be 20 characters or fewer") String> activityRegionCodes,

        @Size(max = 20, message = "availableTimeSlotCodes must contain 20 items or fewer")
        Set<@Size(max = 30, message = "availableTimeSlotCode must be 30 characters or fewer") String> availableTimeSlotCodes,

        @Size(max = 20, message = "projectTypeCodes must contain 20 items or fewer")
        Set<@Size(max = 30, message = "projectTypeCode must be 30 characters or fewer") String> projectTypeCodes
) {
}
