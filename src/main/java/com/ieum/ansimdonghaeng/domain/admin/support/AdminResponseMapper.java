package com.ieum.ansimdonghaeng.domain.admin.support;

import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerSummaryResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminUserSummaryResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.user.entity.User;

public final class AdminResponseMapper {

    private AdminResponseMapper() {
    }

    public static AdminUserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoleCode(),
                user.getActiveYn()
        );
    }

    public static AdminFreelancerSummaryResponse toFreelancerSummary(FreelancerProfile profile) {
        if (profile == null) {
            return null;
        }
        User user = profile.getUser();
        return new AdminFreelancerSummaryResponse(
                profile.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                profile.getVerifiedYn(),
                profile.getPublicYn(),
                user != null ? user.getActiveYn() : null
        );
    }
}
