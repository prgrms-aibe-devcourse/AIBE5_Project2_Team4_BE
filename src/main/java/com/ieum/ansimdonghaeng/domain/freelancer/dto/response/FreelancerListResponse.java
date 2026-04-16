package com.ieum.ansimdonghaeng.domain.freelancer.dto.response;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import java.util.List;
import org.springframework.data.domain.Page;

public record FreelancerListResponse(
        List<PublicFreelancerSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    // 공통 페이지 응답 형식에 맞춰 프리랜서 목록 결과를 감싼다.
    public static FreelancerListResponse from(Page<FreelancerProfile> freelancerPage) {
        return new FreelancerListResponse(
                freelancerPage.getContent().stream()
                        .map(PublicFreelancerSummaryResponse::from)
                        .toList(),
                freelancerPage.getNumber(),
                freelancerPage.getSize(),
                freelancerPage.getTotalElements(),
                freelancerPage.getTotalPages(),
                freelancerPage.hasNext()
        );
    }
}
