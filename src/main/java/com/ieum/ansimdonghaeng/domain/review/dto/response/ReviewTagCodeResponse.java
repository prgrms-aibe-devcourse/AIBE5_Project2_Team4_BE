package com.ieum.ansimdonghaeng.domain.review.dto.response;

import com.ieum.ansimdonghaeng.domain.review.entity.ReviewTagCode;

public record ReviewTagCodeResponse(
        String code,
        String name,
        Integer sortOrder
) {

    public static ReviewTagCodeResponse from(ReviewTagCode reviewTagCode) {
        return new ReviewTagCodeResponse(
                reviewTagCode.getCode(),
                reviewTagCode.getName(),
                reviewTagCode.getSortOrder()
        );
    }
}
