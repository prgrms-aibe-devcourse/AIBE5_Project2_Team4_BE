package com.ieum.ansimdonghaeng.domain.review.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.report.repository.ReportRepository;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewCreateRequest;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewTagCodeRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReviewTagCodeRepository reviewTagCodeRepository;

    @Mock
    private FreelancerService freelancerService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewConvertsUniqueConstraintViolationToReviewAlreadyExists() {
        Long ownerUserId = 1L;
        Long projectId = 10L;
        Project project = Project.create(
                ownerUserId,
                "project",
                "HOSPITAL_COMPANION",
                "SEOUL_GANGNAM",
                LocalDateTime.of(2026, 4, 20, 9, 0),
                LocalDateTime.of(2026, 4, 20, 12, 0),
                "address",
                "detail",
                "request"
        );
        project.accept(LocalDateTime.of(2026, 4, 19, 9, 0));
        project.start(LocalDateTime.of(2026, 4, 20, 9, 0));
        project.complete(LocalDateTime.of(2026, 4, 20, 12, 0));

        User freelancerUser = User.builder()
                .email("freelancer@test.com")
                .passwordHash("encoded")
                .name("freelancer")
                .roleCode(UserRole.FREELANCER.getCode())
                .activeYn(true)
                .build();
        FreelancerProfile freelancerProfile = FreelancerProfile.create(
                freelancerUser,
                "career",
                true,
                true,
                new BigDecimal("4.50"),
                1L,
                true,
                Set.of("SEOUL_GANGNAM"),
                Set.of(),
                Set.of("HOSPITAL_COMPANION")
        );
        Proposal acceptedProposal = Proposal.create(project, freelancerProfile, "accepted");
        acceptedProposal.accept(LocalDateTime.of(2026, 4, 19, 10, 0));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(reviewRepository.existsByProject_Id(projectId)).thenReturn(false);
        when(proposalRepository.findAcceptedProposalByProjectId(projectId)).thenReturn(Optional.of(acceptedProposal));
        when(reviewRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> reviewService.createReview(
                ownerUserId,
                projectId,
                new ReviewCreateRequest(5, java.util.List.of(), "content")
        ))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> org.assertj.core.api.Assertions.assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS));
    }
}
