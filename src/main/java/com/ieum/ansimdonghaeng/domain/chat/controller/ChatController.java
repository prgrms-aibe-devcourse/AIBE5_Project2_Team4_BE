package com.ieum.ansimdonghaeng.domain.chat.controller;

import com.ieum.ansimdonghaeng.common.response.ApiResponse;
import com.ieum.ansimdonghaeng.common.security.AuthenticatedUserSupport;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.chat.dto.request.ChatConversationCreateRequest;
import com.ieum.ansimdonghaeng.domain.chat.dto.request.ChatMessageSendRequest;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatConversationSummaryResponse;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatMessageResponse;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatMessagesResponse;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatReadResponse;
import com.ieum.ansimdonghaeng.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ChatConversationSummaryResponse>>> getConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getMyConversations(AuthenticatedUserSupport.currentUserId(userDetails))
        ));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ChatConversationSummaryResponse>> createConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChatConversationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                chatService.getOrCreateConversation(
                        AuthenticatedUserSupport.currentUserId(userDetails),
                        request.targetUserId(),
                        request.targetFreelancerProfileId()
                )
        ));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessagesResponse>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long conversationId,
            @PositiveOrZero @RequestParam(defaultValue = "0") int page,
            @Positive @RequestParam(defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getMessages(AuthenticatedUserSupport.currentUserId(userDetails), conversationId, page, size)
        ));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageSendRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                chatService.sendMessage(
                        AuthenticatedUserSupport.currentUserId(userDetails),
                        conversationId,
                        request.content()
                )
        ));
    }

    @PatchMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<ChatReadResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long conversationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.markConversationAsRead(AuthenticatedUserSupport.currentUserId(userDetails), conversationId)
        ));
    }
}
