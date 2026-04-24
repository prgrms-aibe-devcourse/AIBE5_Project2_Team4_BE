package com.ieum.ansimdonghaeng.domain.chat.controller;

import com.ieum.ansimdonghaeng.domain.chat.dto.request.ChatMessageSendRequest;
import com.ieum.ansimdonghaeng.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageSocketController {

    private final ChatService chatService;

    @MessageMapping("/chats/{conversationId}/send")
    public void sendMessage(
            @DestinationVariable Long conversationId,
            @Payload @Valid ChatMessageSendRequest request,
            Authentication authentication
    ) {
        com.ieum.ansimdonghaeng.common.security.CustomUserDetails userDetails =
                (com.ieum.ansimdonghaeng.common.security.CustomUserDetails) authentication.getPrincipal();
        chatService.sendMessage(userDetails.getUserId(), conversationId, request.content());
    }
}
