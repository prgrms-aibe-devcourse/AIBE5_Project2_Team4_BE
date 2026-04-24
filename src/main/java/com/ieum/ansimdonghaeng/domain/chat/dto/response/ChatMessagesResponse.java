package com.ieum.ansimdonghaeng.domain.chat.dto.response;

import com.ieum.ansimdonghaeng.common.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public record ChatMessagesResponse(
        List<ChatMessageResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static ChatMessagesResponse from(Page<ChatMessageResponse> page) {
        PageResponse<ChatMessageResponse> response = PageResponse.from(page);
        return new ChatMessagesResponse(
                response.content(),
                response.page(),
                response.size(),
                response.totalElements(),
                response.totalPages(),
                response.hasNext()
        );
    }
}
