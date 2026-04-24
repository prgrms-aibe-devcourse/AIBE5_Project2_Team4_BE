package com.ieum.ansimdonghaeng.domain.chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.common.jwt.JwtTokenProvider;
import com.ieum.ansimdonghaeng.domain.chat.entity.ChatConversation;
import com.ieum.ansimdonghaeng.domain.chat.repository.ChatConversationRepository;
import com.ieum.ansimdonghaeng.domain.chat.repository.ChatMessageRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatConversationRepository chatConversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatConversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("user and freelancer can create conversation and exchange messages")
    void createConversationAndSendMessage() throws Exception {
        User user = userRepository.save(createUser("user-chat@test.com", "user", "ROLE_USER"));
        User freelancer = userRepository.save(createUser("freelancer-chat@test.com", "freelancer", "ROLE_FREELANCER"));

        String response = mockMvc.perform(post("/api/v1/chats/conversations")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetUserId", freelancer.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.otherUserId").value(freelancer.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long conversationId = objectMapper.readTree(response).path("data").path("conversationId").asLong();

        mockMvc.perform(post("/api/v1/chats/{conversationId}/messages", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "hello freelancer"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderUserId").value(user.getId()))
                .andExpect(jsonPath("$.data.content").value("hello freelancer"));

        mockMvc.perform(get("/api/v1/chats/{conversationId}/messages", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].content").value("hello freelancer"));

        mockMvc.perform(patch("/api/v1/chats/{conversationId}/read", conversationId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.readCount").value(1));

        mockMvc.perform(get("/api/v1/chats/conversations")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].lastMessage").value("hello freelancer"))
                .andExpect(jsonPath("$.data[0].unreadCount").value(0));
    }

    @Test
    @DisplayName("same role participants cannot create conversation")
    void createConversationFailsForSameRole() throws Exception {
        User freelancerA = userRepository.save(createUser("freelancer-a@test.com", "freelancer-a", "ROLE_FREELANCER"));
        User freelancerB = userRepository.save(createUser("freelancer-b@test.com", "freelancer-b", "ROLE_FREELANCER"));

        mockMvc.perform(post("/api/v1/chats/conversations")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(freelancerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetUserId", freelancerB.getId()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_403"));
    }

    @Test
    @DisplayName("non participant cannot read other conversation")
    void getMessagesFailsForNonParticipant() throws Exception {
        User user = userRepository.save(createUser("user-owner@test.com", "user", "ROLE_USER"));
        User freelancer = userRepository.save(createUser("freelancer-owner@test.com", "freelancer", "ROLE_FREELANCER"));
        User stranger = userRepository.save(createUser("user-stranger@test.com", "stranger", "ROLE_USER"));

        ChatConversation conversation = chatConversationRepository.save(ChatConversation.create(user, freelancer));

        mockMvc.perform(get("/api/v1/chats/{conversationId}/messages", conversation.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(stranger)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_403"));
    }

    private User createUser(String email, String name, String roleCode) {
        return User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("1234"))
                .name(name)
                .phone("010-0000-0000")
                .intro("intro")
                .roleCode(roleCode)
                .activeYn(true)
                .build();
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                List.of(new SimpleGrantedAuthority(user.getRoleCode()))
        );
    }
}
