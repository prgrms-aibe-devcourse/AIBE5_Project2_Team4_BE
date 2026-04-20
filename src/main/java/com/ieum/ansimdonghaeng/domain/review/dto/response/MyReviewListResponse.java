package com.ieum.ansimdonghaeng.domain.review.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record MyReviewListResponse(
        List<MyReviewResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static MyReviewListResponse from(Page<MyReviewResponse> page) {
        return new MyReviewListResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
