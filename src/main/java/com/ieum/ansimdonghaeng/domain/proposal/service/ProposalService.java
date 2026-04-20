package com.ieum.ansimdonghaeng.domain.proposal.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.notification.service.NotificationService;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.dto.request.ProposalCreateRequest;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProjectProposalSummaryResponse;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalCreateResponse;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalDetailResponse;
import com.ieum.ansimdonghaeng.domain.proposal.dto.response.ProposalListResponse;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProjectProposalSummaryView;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalSummaryView;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalService {

    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public ProposalCreateResponse createProposal(Long currentUserId, Long projectId, ProposalCreateRequest request) {
        Project project = getOwnedProject(projectId, currentUserId);
        validateRequestedProject(project);

        FreelancerProfile freelancerProfile = getAvailableFreelancerProfile(request.freelancerProfileId());
        if (proposalRepository.existsByProject_IdAndFreelancerProfile_Id(projectId, freelancerProfile.getId())) {
            throw new CustomException(ErrorCode.PROPOSAL_DUPLICATE);
        }

        Proposal proposal = Proposal.create(project, freelancerProfile, request.message());
        try {
            Proposal savedProposal = proposalRepository.saveAndFlush(proposal);
            notificationService.notifyProposalReceived(savedProposal);
            return ProposalCreateResponse.from(savedProposal);
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(ErrorCode.PROPOSAL_DUPLICATE);
        }
    }

    public PageResponse<ProjectProposalSummaryResponse> getProjectProposals(Long currentUserId,
                                                                            Long projectId,
                                                                            ProposalStatus status,
                                                                            int page,
                                                                            int size) {
        getOwnedProject(projectId, currentUserId);
        Page<ProjectProposalSummaryView> proposalPage = proposalRepository.findProjectOwnerProposals(
                projectId,
                status,
                PageRequest.of(page, size)
        );
        return PageResponse.from(proposalPage.map(ProjectProposalSummaryResponse::from));
    }

    public ProposalListResponse getMyProposals(Long currentUserId, ProposalStatus status, int page, int size) {
        FreelancerProfile currentFreelancerProfile = getCurrentFreelancerProfile(currentUserId);
        Page<ProposalSummaryView> proposalPage = proposalRepository.findFreelancerProposals(
                currentFreelancerProfile.getId(),
                status,
                PageRequest.of(page, size)
        );
        return ProposalListResponse.from(proposalPage);
    }

    public ProposalDetailResponse getMyProposal(Long currentUserId, Long proposalId) {
        FreelancerProfile currentFreelancerProfile = getCurrentFreelancerProfile(currentUserId);
        Proposal proposal = proposalRepository.findDetailById(proposalId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPOSAL_NOT_FOUND));

        validateProposalOwnership(proposal, currentFreelancerProfile.getId());
        return ProposalDetailResponse.from(proposal);
    }

    @Transactional
    public ProposalDetailResponse acceptProposal(Long currentUserId, Long proposalId) {
        FreelancerProfile currentFreelancerProfile = getCurrentFreelancerProfile(currentUserId);
        Proposal proposal = proposalRepository.findDetailByIdForUpdate(proposalId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPOSAL_NOT_FOUND));

        validateProposalOwnership(proposal, currentFreelancerProfile.getId());
        if (!proposal.isPendingStatus()) {
            throw new CustomException(ErrorCode.PROPOSAL_INVALID_STATUS);
        }

        Project project = projectRepository.findByIdForUpdate(proposal.getProject().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        validateRequestedProject(project);

        LocalDateTime now = LocalDateTime.now();
        proposal.accept(now);
        project.accept(now);

        List<Proposal> pendingProposals = proposalRepository.findAllByProject_IdAndStatusAndIdNot(
                project.getId(),
                ProposalStatus.PENDING,
                proposal.getId()
        );
        pendingProposals.forEach(pendingProposal -> pendingProposal.reject(now));

        notificationService.notifyProposalAccepted(proposal);
        notificationService.notifyProjectStatusChanged(project, proposal);

        return ProposalDetailResponse.from(proposal);
    }

    @Transactional
    public ProposalDetailResponse rejectProposal(Long currentUserId, Long proposalId) {
        FreelancerProfile currentFreelancerProfile = getCurrentFreelancerProfile(currentUserId);
        Proposal proposal = proposalRepository.findDetailByIdForUpdate(proposalId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPOSAL_NOT_FOUND));

        validateProposalOwnership(proposal, currentFreelancerProfile.getId());
        if (!proposal.isPendingStatus()) {
            throw new CustomException(ErrorCode.PROPOSAL_INVALID_STATUS);
        }

        proposal.reject(LocalDateTime.now());
        return ProposalDetailResponse.from(proposal);
    }

    private Project getOwnedProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        return project;
    }

    private FreelancerProfile getAvailableFreelancerProfile(Long freelancerProfileId) {
        FreelancerProfile freelancerProfile = freelancerProfileRepository.findDetailById(freelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));

        if (!freelancerProfile.isPublicProfile()
                || Boolean.FALSE.equals(freelancerProfile.getUser().getActiveYn())
                || !UserRole.FREELANCER.getCode().equals(freelancerProfile.getUser().getRoleCode())) {
            throw new CustomException(ErrorCode.FREELANCER_NOT_FOUND);
        }

        return freelancerProfile;
    }

    private FreelancerProfile getCurrentFreelancerProfile(Long currentUserId) {
        FreelancerProfile freelancerProfile = freelancerProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));

        if (!UserRole.FREELANCER.getCode().equals(freelancerProfile.getUser().getRoleCode())
                || Boolean.FALSE.equals(freelancerProfile.getUser().getActiveYn())) {
            throw new CustomException(ErrorCode.FREELANCER_NOT_FOUND);
        }
        return freelancerProfile;
    }

    private void validateProposalOwnership(Proposal proposal, Long freelancerProfileId) {
        if (!proposal.isOwnedByFreelancerProfile(freelancerProfileId)) {
            throw new CustomException(ErrorCode.PROPOSAL_ACCESS_DENIED);
        }
    }

    private void validateRequestedProject(Project project) {
        if (project.getStatus() != ProjectStatus.REQUESTED) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_STATUS);
        }
    }
}
