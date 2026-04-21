package com.ieum.ansimdonghaeng.domain.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ieum.ansimdonghaeng.domain.auth.dto.response.KakaoUserInfo;
import com.ieum.ansimdonghaeng.domain.auth.oauth.KakaoOAuthClient;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KakaoOAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FreelancerProfileRepository freelancerProfileRepository;

    @MockBean
    private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        freelancerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("kakao oauth endpoint returns service token response")
    void kakaoOAuthEndpointReturnsTokenResponse() throws Exception {
        when(kakaoOAuthClient.getUserInfo("kakao-access-token"))
                .thenReturn(new KakaoUserInfo("12345", "kakao-controller@test.com", "kakao-controller"));

        mockMvc.perform(post("/api/v1/auth/oauth/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new KakaoOAuthLoginRequest("kakao-access-token", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("kakao-controller@test.com"))
                .andExpect(jsonPath("$.data.user.roleCode").value("ROLE_USER"));
    }
}
