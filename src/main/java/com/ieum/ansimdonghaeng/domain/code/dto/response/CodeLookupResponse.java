package com.ieum.ansimdonghaeng.domain.code.dto.response;

public record CodeLookupResponse(
        String code,
        String name,
        Integer sortOrder,
        String parentRegionCode,
        Integer regionLevel,
        Integer startMinute,
        Integer endMinute
) {
}
