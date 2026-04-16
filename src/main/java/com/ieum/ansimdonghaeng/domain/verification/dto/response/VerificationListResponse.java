package com.ieum.ansimdonghaeng.domain.verification.dto.response;

import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationRequest;
import java.util.List;
import org.springframework.data.domain.Page;

public record VerificationListResponse(
        List<VerificationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static VerificationListResponse from(Page<VerificationRequest> verificationPage) {
        return new VerificationListResponse(
                verificationPage.getContent().stream()
                        .map(VerificationResponse::from)
                        .toList(),
                verificationPage.getNumber(),
                verificationPage.getSize(),
                verificationPage.getTotalElements(),
                verificationPage.getTotalPages(),
                verificationPage.hasNext()
        );
    }
}
