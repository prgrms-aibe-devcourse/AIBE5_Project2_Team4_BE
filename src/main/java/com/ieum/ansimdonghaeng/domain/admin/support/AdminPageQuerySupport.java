package com.ieum.ansimdonghaeng.domain.admin.support;

import com.ieum.ansimdonghaeng.common.response.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class AdminPageQuerySupport {

    private AdminPageQuerySupport() {
    }

    public static String orderByClause(Pageable pageable,
                                       Map<String, String> sortMappings,
                                       String defaultClause) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return defaultClause;
        }

        String orderBy = pageable.getSort().stream()
                .map(order -> toOrderExpression(order, sortMappings))
                .filter(expression -> expression != null && !expression.isBlank())
                .collect(Collectors.joining(", "));

        return orderBy.isBlank() ? defaultClause : orderBy;
    }

    public static <T> PageResponse<T> toPageResponse(List<T> content, Pageable pageable, long totalElements) {
        int size = pageable.getPageSize();
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = pageable.getOffset() + content.size() < totalElements;
        return new PageResponse<>(
                content,
                pageable.getPageNumber(),
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }

    private static String toOrderExpression(Sort.Order order, Map<String, String> sortMappings) {
        String mappedProperty = sortMappings.get(order.getProperty());
        if (mappedProperty == null) {
            return null;
        }
        return mappedProperty + (order.isAscending() ? " asc" : " desc");
    }
}
