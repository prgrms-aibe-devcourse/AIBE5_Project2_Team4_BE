package com.ieum.ansimdonghaeng.domain.chat.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatConversationSummaryResponse;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatMessageResponse;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatMessagesResponse;
import com.ieum.ansimdonghaeng.domain.chat.dto.response.ChatReadResponse;
import com.ieum.ansimdonghaeng.domain.chat.entity.ChatConversation;
import com.ieum.ansimdonghaeng.domain.chat.entity.ChatMessage;
import com.ieum.ansimdonghaeng.domain.chat.repository.ChatConversationRepository;
import com.ieum.ansimdonghaeng.domain.chat.repository.ChatMessageRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public ChatConversationSummaryResponse getOrCreateConversation(Long currentUserId,
                                                                   Long targetUserId,
                                                                   Long targetFreelancerProfileId) {
        User currentUser = getActiveUser(currentUserId);
        User targetUser = getActiveUser(resolveTargetUserId(targetUserId, targetFreelancerProfileId));
        validateParticipants(currentUser, targetUser);

        ChatConversation conversation = findOrCreateConversation(currentUser, targetUser);
        String lastMessage = findLastMessage(conversation.getId());
        long unreadCount = chatMessageRepository.countByConversation_IdAndSender_IdNotAndReadYnFalse(
                conversation.getId(),
                currentUserId
        );
        return ChatConversationSummaryResponse.of(conversation, conversation.otherParticipant(currentUserId), lastMessage, unreadCount);
    }

    public List<ChatConversationSummaryResponse> getMyConversations(Long currentUserId) {
        User currentUser = getActiveUser(currentUserId);
        List<ChatConversation> conversations = chatConversationRepository.findAllByParticipantUserId(currentUserId);
        if (conversations.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> unreadCountByConversationId = new HashMap<>();
        chatMessageRepository.findAllByConversation_IdInAndSender_IdNotAndReadYnFalse(
                        conversations.stream().map(ChatConversation::getId).toList(),
                        currentUserId
                ).forEach(message -> unreadCountByConversationId.merge(message.getConversation().getId(), 1L, Long::sum));

        Map<Long, String> lastMessageByConversationId = new HashMap<>();
        for (ChatConversation conversation : conversations) {
            lastMessageByConversationId.put(conversation.getId(), findLastMessage(conversation.getId()));
        }

        return conversations.stream()
                .map(conversation -> ChatConversationSummaryResponse.of(
                        conversation,
                        conversation.otherParticipant(currentUser.getId()),
                        lastMessageByConversationId.get(conversation.getId()),
                        unreadCountByConversationId.getOrDefault(conversation.getId(), 0L)
                ))
                .toList();
    }

    public ChatMessagesResponse getMessages(Long currentUserId, Long conversationId, int page, int size) {
        ChatConversation conversation = getOwnedConversation(currentUserId, conversationId);
        Page<ChatMessageResponse> messagePage = chatMessageRepository.findAllByConversation_IdOrderByCreatedAtDescIdDesc(
                        conversation.getId(),
                        PageRequest.of(page, size)
                )
                .map(ChatMessageResponse::from);
        return ChatMessagesResponse.from(messagePage);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long currentUserId, Long conversationId, String rawContent) {
        User sender = getActiveUser(currentUserId);
        ChatConversation conversation = getOwnedConversation(currentUserId, conversationId);
        String content = normalizeContent(rawContent);

        ChatMessage message = chatMessageRepository.save(ChatMessage.create(conversation, sender, content));
        conversation.touch(message.getCreatedAt());

        ChatMessageResponse response = ChatMessageResponse.from(message);
        publishMessage(conversation, response);
        return response;
    }

    @Transactional
    public ChatReadResponse markConversationAsRead(Long currentUserId, Long conversationId) {
        getOwnedConversation(currentUserId, conversationId);
        int readCount = chatMessageRepository.markConversationAsRead(conversationId, currentUserId, LocalDateTime.now());
        return new ChatReadResponse(conversationId, readCount);
    }

    private Long resolveTargetUserId(Long targetUserId, Long targetFreelancerProfileId) {
        boolean hasTargetUserId = targetUserId != null;
        boolean hasTargetFreelancerProfileId = targetFreelancerProfileId != null;
        if (hasTargetUserId == hasTargetFreelancerProfileId) {
            throw new CustomException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Provide exactly one of targetUserId or targetFreelancerProfileId."
            );
        }

        if (hasTargetUserId) {
            return targetUserId;
        }

        FreelancerProfile profile = freelancerProfileRepository.findDetailById(targetFreelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));
        validateTargetFreelancerProfile(profile);
        return profile.getUser().getId();
    }

    private void validateTargetFreelancerProfile(FreelancerProfile profile) {
        if (!profile.isPublicProfile()
                || Boolean.FALSE.equals(profile.getUser().getActiveYn())
                || profile.getUser().getRole() != UserRole.FREELANCER) {
            throw new CustomException(ErrorCode.FREELANCER_NOT_FOUND);
        }
    }

    private ChatConversation findOrCreateConversation(User currentUser, User targetUser) {
        Long lowerUserId = Math.min(currentUser.getId(), targetUser.getId());
        Long higherUserId = Math.max(currentUser.getId(), targetUser.getId());

        return chatConversationRepository.findByParticipantA_IdAndParticipantB_Id(lowerUserId, higherUserId)
                .orElseGet(() -> chatConversationRepository.save(ChatConversation.create(currentUser, targetUser)));
    }

    private ChatConversation getOwnedConversation(Long currentUserId, Long conversationId) {
        ChatConversation conversation = chatConversationRepository.findDetailById(conversationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Chat conversation was not found."));
        if (!conversation.involves(currentUserId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "You do not have access to this chat conversation.");
        }
        return conversation;
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));
        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }
        return user;
    }

    private void validateParticipants(User currentUser, User targetUser) {
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "targetUserId must be different from current user.");
        }
        if (currentUser.getRole() == UserRole.ADMIN || targetUser.getRole() == UserRole.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN, "Admin users cannot participate in chat conversations.");
        }

        boolean validPair =
                (currentUser.getRole() == UserRole.USER && targetUser.getRole() == UserRole.FREELANCER)
                        || (currentUser.getRole() == UserRole.FREELANCER && targetUser.getRole() == UserRole.USER);
        if (!validPair) {
            throw new CustomException(ErrorCode.FORBIDDEN, "Chat is only available between users and freelancers.");
        }
    }

    private String normalizeContent(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "content is required.");
        }
        String content = rawContent.trim();
        if (content.length() > 2000) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "content must be 2000 characters or fewer.");
        }
        return content;
    }

    private String findLastMessage(Long conversationId) {
        ChatMessage message = chatMessageRepository.findTop1ByConversation_IdOrderByCreatedAtDescIdDesc(conversationId);
        return message == null ? null : message.getContent();
    }

    private void publishMessage(ChatConversation conversation, ChatMessageResponse response) {
        simpMessagingTemplate.convertAndSendToUser(
                conversation.getParticipantA().getEmail(),
                "/queue/chats",
                response
        );
        if (!conversation.getParticipantA().getId().equals(conversation.getParticipantB().getId())) {
            simpMessagingTemplate.convertAndSendToUser(
                    conversation.getParticipantB().getEmail(),
                    "/queue/chats",
                    response
            );
        }
    }
}
