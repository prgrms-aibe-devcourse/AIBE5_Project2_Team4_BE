package com.ieum.ansimdonghaeng.domain.project;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @AfterEach
    void tearDown() {
        // 제안이 프로젝트를 참조하므로 자식 테이블부터 정리한다.
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void createProjectSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "병원 동행 요청",
                                "projectTypeCode", "HOSPITAL_COMPANION",
                                "serviceRegionCode", "SEOUL_GANGNAM",
                                "requestedStartAt", "2026-04-10T14:00:00",
                                "requestedEndAt", "2026-04-10T17:00:00",
                                "serviceAddress", "서울 강남구 테헤란로 123",
                                "serviceDetailAddress", "3층 접수대 앞",
                                "requestDetail", "병원 접수 및 진료 동행이 필요합니다."
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").isNumber())
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
    }

    @Test
    void createProjectValidationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", " ",
                                "projectTypeCode", "HOSPITAL_COMPANION",
                                "serviceRegionCode", "SEOUL_GANGNAM",
                                "requestedStartAt", "2026-04-10T14:00:00",
                                "requestedEndAt", "2026-04-10T17:00:00",
                                "serviceAddress", "서울 강남구 테헤란로 123",
                                "requestDetail", "병원 접수 및 진료 동행이 필요합니다."
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    void createProjectRejectsUnsupportedProjectTypeCode() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "invalid project type",
                                "projectTypeCode", "UNKNOWN_TYPE",
                                "serviceRegionCode", "SEOUL_GANGNAM",
                                "requestedStartAt", "2026-04-10T14:00:00",
                                "requestedEndAt", "2026-04-10T17:00:00",
                                "serviceAddress", "Seoul Gangnam 123",
                                "requestDetail", "invalid code"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"))
                .andExpect(jsonPath("$.error.message").value("projectTypeCode: unsupported code(s) [UNKNOWN_TYPE]"));
    }

    @Test
    void listMyProjectsSuccess() throws Exception {
        Project first = persistProject(1L, "첫 번째 프로젝트", ProjectStatus.REQUESTED);
        Project second = persistProject(1L, "두 번째 프로젝트", ProjectStatus.CANCELLED);
        persistProject(2L, "다른 사용자 프로젝트", ProjectStatus.REQUESTED);

        mockMvc.perform(get("/api/v1/projects/me")
                        .with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.content[*].projectId",
                        containsInAnyOrder(first.getId().intValue(), second.getId().intValue())));
    }

    @Test
    void listMyProjectsWithStatusFilter() throws Exception {
        Project requested = persistProject(1L, "요청 상태 프로젝트", ProjectStatus.REQUESTED);
        persistProject(1L, "취소 상태 프로젝트", ProjectStatus.CANCELLED);

        mockMvc.perform(get("/api/v1/projects/me")
                        .with(authenticatedUser(1L))
                        .param("status", "REQUESTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].projectId").value(requested.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value("REQUESTED"));
    }

    @Test
    void getProjectSuccess() throws Exception {
        Project project = persistProject(1L, "상세 조회 프로젝트", ProjectStatus.REQUESTED);

        mockMvc.perform(get("/api/v1/projects/{projectId}", project.getId())
                        .with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(project.getId()))
                .andExpect(jsonPath("$.data.ownerUserId").value(1))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
    }

    @Test
    void getProjectForbiddenForNonOwner() throws Exception {
        Project project = persistProject(1L, "권한 체크 프로젝트", ProjectStatus.REQUESTED);

        mockMvc.perform(get("/api/v1/projects/{projectId}", project.getId())
                        .with(authenticatedUser(2L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_403_1"));
    }

    @Test
    void updateProjectSuccess() throws Exception {
        Project project = persistProject(1L, "수정 전 제목", ProjectStatus.REQUESTED);

        mockMvc.perform(patch("/api/v1/projects/{projectId}", project.getId())
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "수정 후 제목",
                                "requestedEndAt", "2026-04-10T18:00:00",
                                "requestDetail", "수정된 요청 상세"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정 후 제목"))
                .andExpect(jsonPath("$.data.requestedEndAt").value("2026-04-10T18:00:00"))
                .andExpect(jsonPath("$.data.requestDetail").value("수정된 요청 상세"))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
    }

    @Test
    void updateProjectFailsWhenStatusIsNotRequested() throws Exception {
        Project project = persistProject(1L, "수정 불가 프로젝트", ProjectStatus.ACCEPTED);

        mockMvc.perform(patch("/api/v1/projects/{projectId}", project.getId())
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "변경 시도"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_400_1"));
    }

    @Test
    void cancelProjectSuccess() throws Exception {
        Project project = persistProject(1L, "취소 대상 프로젝트", ProjectStatus.REQUESTED);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/cancel", project.getId())
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reason", "개인 일정 변경으로 취소합니다."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectId").value(project.getId()))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelledReason").value("개인 일정 변경으로 취소합니다."))
                .andExpect(jsonPath("$.data.cancelledAt").exists());
    }

    @Test
    void cancelProjectFailsWhenStatusIsNotRequested() throws Exception {
        Project project = persistProject(1L, "취소 불가 프로젝트", ProjectStatus.ACCEPTED);

        mockMvc.perform(patch("/api/v1/projects/{projectId}/cancel", project.getId())
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reason", "개인 일정 변경으로 취소합니다."
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_400_1"));
    }

    @Test
    void getProjectNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/projects/{projectId}", 99999L)
                        .with(authenticatedUser(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_404_1"));
    }

    @Test
    void updateProjectNotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/projects/{projectId}", 99999L)
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "변경 시도"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_404_1"));
    }

    @Test
    void cancelProjectNotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/projects/{projectId}/cancel", 99999L)
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reason", "개인 일정 변경으로 취소합니다."
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_404_1"));
    }

    private Project persistProject(Long ownerUserId, String title, ProjectStatus status) {
        Project project = Project.create(
                ownerUserId,
                title,
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 10, 14, 0),
                LocalDateTime.of(2026, 4, 10, 17, 0),
                "서울 강남구 테헤란로 123",
                "3층 접수대 앞",
                "병원 접수 및 진료 동행이 필요합니다."
        );

        Project savedProject = projectRepository.saveAndFlush(project);
        if (status != ProjectStatus.REQUESTED) {
            ReflectionTestUtils.setField(savedProject, "status", status);
        }
        if (status == ProjectStatus.CANCELLED) {
            ReflectionTestUtils.setField(savedProject, "cancelledAt", LocalDateTime.of(2026, 4, 1, 10, 0));
            ReflectionTestUtils.setField(savedProject, "cancelledReason", "사전 취소");
        }

        return projectRepository.saveAndFlush(savedProject);
    }

    private RequestPostProcessor authenticatedUser(Long userId) {
        return user(CustomUserDetails.builder()
                .userId(userId)
                .username("user" + userId)
                .password("{noop}password")
                .authorities(List.of(new SimpleGrantedAuthority(UserRole.USER.asAuthority())))
                .enabled(true)
                .build());
    }
}
