package com.ieum.ansimdonghaeng.domain.review.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.support.AdminIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest extends AdminIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reviewUsesCanonicalTagCodesAcrossCreateUpdateAndList() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, freelancerProfile);

        MvcResult createResult = mockMvc.perform(post("/api/v1/projects/{projectId}/reviews", project.getId())
                        .with(userPrincipal(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 5,
                                "tagCodes", List.of("KIND", "PUNCTUAL"),
                                "content", "great support"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tagCodes", containsInAnyOrder("KIND", "PUNCTUAL")))
                .andReturn();

        long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("reviewId")
                .asLong();

        mockMvc.perform(get("/api/v1/users/me/reviews").with(userPrincipal(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].tagCodes", containsInAnyOrder("KIND", "PUNCTUAL")));

        mockMvc.perform(patch("/api/v1/users/me/reviews/{reviewId}", reviewId)
                        .with(userPrincipal(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 4,
                                "tagCodes", List.of("PUNCTUAL"),
                                "content", "updated review"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagCodes[0]").value("PUNCTUAL"))
                .andExpect(jsonPath("$.data.rating").value(4));

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].tagCodes[0]").value("PUNCTUAL"));

        mockMvc.perform(get("/api/v1/reviews/tag-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("KIND"));
    }

    @Test
    void freelancerCanReviewRequesterForCompletedAssignedProject() throws Exception {
        User owner = saveUser("owner-reverse@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer-reverse@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, freelancerProfile);

        mockMvc.perform(post("/api/v1/projects/{projectId}/reviews", project.getId())
                        .with(userPrincipal(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 5,
                                "tagCodes", List.of("KIND"),
                                "content", "great freelancer"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reviewDirection").value("USER_TO_FREELANCER"))
                .andExpect(jsonPath("$.data.revieweeUserId").value(freelancerUser.getId()));

        MvcResult reverseResult = mockMvc.perform(post("/api/v1/projects/{projectId}/requester-reviews", project.getId())
                        .with(freelancerPrincipal(freelancerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 4,
                                "tagCodes", List.of("PUNCTUAL"),
                                "content", "clear request"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reviewDirection").value("FREELANCER_TO_USER"))
                .andExpect(jsonPath("$.data.reviewerUserId").value(freelancerUser.getId()))
                .andExpect(jsonPath("$.data.revieweeUserId").value(owner.getId()))
                .andExpect(jsonPath("$.data.rating").value(4))
                .andReturn();

        long reverseReviewId = objectMapper.readTree(reverseResult.getResponse().getContentAsString())
                .path("data")
                .path("reviewId")
                .asLong();

        mockMvc.perform(get("/api/v1/users/me/reviews").with(freelancerPrincipal(freelancerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(reverseReviewId))
                .andExpect(jsonPath("$.data.content[0].reviewDirection").value("FREELANCER_TO_USER"));

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reviewDirection").value("USER_TO_FREELANCER"));
    }

    @Test
    void requesterReviewFailsForUnassignedFreelancer() throws Exception {
        User owner = saveUser("owner-denied@test.com", "owner", UserRole.USER);
        User assignedFreelancerUser = saveUser("assigned-freelancer@test.com", "assigned", UserRole.FREELANCER);
        User otherFreelancerUser = saveUser("other-freelancer@test.com", "other", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(assignedFreelancerUser, true, true);
        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, freelancerProfile);

        mockMvc.perform(post("/api/v1/projects/{projectId}/requester-reviews", project.getId())
                        .with(freelancerPrincipal(otherFreelancerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 4,
                                "tagCodes", List.of("KIND"),
                                "content", "not assigned"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("PROJECT_403_1"));
    }

    @Test
    void publicFreelancerReviewsReturnOnlyVisibleAcceptedReviews() throws Exception {
        User owner = saveUser("owner@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);

        var visibleProject = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(visibleProject, freelancerProfile);
        var visibleReview = saveReview(visibleProject, 5, false);

        var blindedProject = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(blindedProject, freelancerProfile);
        saveReview(blindedProject, 4, true);

        var notAcceptedProject = saveProject(owner, ProjectStatus.COMPLETED);
        proposalRepository.saveAndFlush(com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal.create(
                notAcceptedProject,
                freelancerProfile,
                "pending"
        ));
        saveReview(notAcceptedProject, 3, false);

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(visibleReview.getId()));
    }

    @Test
    void freelancerListAndDetailReflectRecalculatedStatsAfterReviewChanges() throws Exception {
        User owner = saveUser("owner-stats@test.com", "owner", UserRole.USER);
        User freelancerUser = saveUser("freelancer-stats@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);
        freelancerProfile.updateStats(java.math.BigDecimal.ZERO, 0L);
        freelancerProfileRepository.saveAndFlush(freelancerProfile);

        var project = saveProject(owner, ProjectStatus.COMPLETED);
        saveAcceptedProposal(project, freelancerProfile);

        MvcResult createResult = mockMvc.perform(post("/api/v1/projects/{projectId}/reviews", project.getId())
                        .with(userPrincipal(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 5,
                                "tagCodes", List.of("KIND"),
                                "content", "great freelancer"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("reviewId")
                .asLong();

        mockMvc.perform(get("/api/v1/freelancers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].activityCount").value(1))
                .andExpect(jsonPath("$.data.content[0].averageRating", closeTo(5.0, 0.001)));

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}", freelancerProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityCount").value(1))
                .andExpect(jsonPath("$.data.averageRating", closeTo(5.0, 0.001)));

        mockMvc.perform(patch("/api/v1/users/me/reviews/{reviewId}", reviewId)
                        .with(userPrincipal(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "rating", 3,
                                "tagCodes", List.of("KIND"),
                                "content", "updated review"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/freelancers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].averageRating", closeTo(3.0, 0.001)));

        mockMvc.perform(delete("/api/v1/users/me/reviews/{reviewId}", reviewId)
                        .with(userPrincipal(owner)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}", freelancerProfile.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityCount").value(1))
                .andExpect(jsonPath("$.data.averageRating", closeTo(0.0, 0.001)));
    }

    @Test
    void privateFreelancerReturnsNotFoundForPublicReviewEndpoint() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, false);

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfile.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FREELANCER_404_1"));
    }

    @Test
    void inactiveFreelancerReturnsNotFoundForPublicReviewEndpoint() throws Exception {
        User freelancerUser = saveUser("freelancer@test.com", "freelancer", UserRole.FREELANCER, false);
        var freelancerProfile = saveFreelancerProfile(freelancerUser, true, true);

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfile.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FREELANCER_404_1"));
    }

    @Test
    void roleMismatchFreelancerProfileReturnsNotFoundForPublicReviewEndpoint() throws Exception {
        User user = saveUser("user@test.com", "user", UserRole.USER);
        var freelancerProfile = saveFreelancerProfile(user, true, true);

        mockMvc.perform(get("/api/v1/freelancers/{freelancerProfileId}/reviews", freelancerProfile.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FREELANCER_404_1"));
    }
}
