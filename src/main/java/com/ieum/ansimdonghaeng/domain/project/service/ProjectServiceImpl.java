package com.ieum.ansimdonghaeng.domain.project.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.code.service.CodeValidationService;
import com.ieum.ansimdonghaeng.domain.notification.service.NotificationService;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCancelRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCreateRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectUpdateRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectCancelResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectCreateResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectDetailResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectListResponse;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectSummaryView;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CodeValidationService codeValidationService;
    private final ProposalRepository proposalRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ProjectCreateResponse createProject(Long currentUserId, ProjectCreateRequest request) {
        validateTimeRange(request.requestedStartAt(), request.requestedEndAt());
        validateProjectCodes(request.projectTypeCode(), request.serviceRegionCode());

        Project project = Project.create(
                currentUserId,
                request.title(),
                request.projectTypeCode(),
                request.serviceRegionCode(),
                request.requestedStartAt(),
                request.requestedEndAt(),
                request.serviceAddress(),
                request.serviceDetailAddress(),
                request.requestDetail()
        );

        return ProjectCreateResponse.from(projectRepository.save(project));
    }

    @Override
    public ProjectListResponse getMyProjects(Long currentUserId, ProjectStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectSummaryView> projectPage = projectRepository.findMyProjects(currentUserId, status, pageable);
        return ProjectListResponse.from(projectPage);
    }

    @Override
    public ProjectDetailResponse getProject(Long currentUserId, Long projectId) {
        return ProjectDetailResponse.from(getOwnedProject(projectId, currentUserId));
    }

    @Override
    @Transactional
    public ProjectDetailResponse updateProject(Long currentUserId, Long projectId, ProjectUpdateRequest request) {
        if (!request.hasChanges()) {
            throw new CustomException(ErrorCode.PROJECT_UPDATE_EMPTY);
        }

        Project project = getOwnedProject(projectId, currentUserId);
        validateRequestedStatus(project);

        LocalDateTime requestedStartAt = request.requestedStartAt() != null
                ? request.requestedStartAt()
                : project.getRequestedStartAt();
        LocalDateTime requestedEndAt = request.requestedEndAt() != null
                ? request.requestedEndAt()
                : project.getRequestedEndAt();
        validateTimeRange(requestedStartAt, requestedEndAt);
        validateProjectCodes(
                request.projectTypeCode() != null ? request.projectTypeCode() : project.getProjectTypeCode(),
                request.serviceRegionCode() != null ? request.serviceRegionCode() : project.getServiceRegionCode()
        );

        project.update(
                request.title() != null ? request.title() : project.getTitle(),
                request.projectTypeCode() != null ? request.projectTypeCode() : project.getProjectTypeCode(),
                request.serviceRegionCode() != null ? request.serviceRegionCode() : project.getServiceRegionCode(),
                requestedStartAt,
                requestedEndAt,
                request.serviceAddress() != null ? request.serviceAddress() : project.getServiceAddress(),
                request.serviceDetailAddress() != null
                        ? request.serviceDetailAddress()
                        : project.getServiceDetailAddress(),
                request.requestDetail() != null ? request.requestDetail() : project.getRequestDetail()
        );

        return ProjectDetailResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectCancelResponse cancelProject(Long currentUserId, Long projectId, ProjectCancelRequest request) {
        Project project = getOwnedProject(projectId, currentUserId);
        validateRequestedStatus(project);

        project.cancel(request.reason(), LocalDateTime.now());
        return ProjectCancelResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectDetailResponse startProject(Long currentUserId, boolean adminOverride, Long projectId) {
        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        if (!project.isAcceptedStatus()) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_STATUS);
        }

        Proposal acceptedProposal = getAcceptedProposal(projectId);
        validateAssignedFreelancer(currentUserId, adminOverride, acceptedProposal);

        project.start(LocalDateTime.now());
        notificationService.notifyProjectStatusChanged(project, acceptedProposal);
        return ProjectDetailResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectDetailResponse completeProject(Long currentUserId, boolean adminOverride, Long projectId) {
        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        if (!project.isInProgressStatus()) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_STATUS);
        }

        Proposal acceptedProposal = getAcceptedProposal(projectId);
        validateAssignedFreelancer(currentUserId, adminOverride, acceptedProposal);

        project.complete(LocalDateTime.now());
        notificationService.notifyProjectStatusChanged(project, acceptedProposal);
        notificationService.notifyReviewRequest(project);
        return ProjectDetailResponse.from(project);
    }

    private Project getOwnedProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return project;
    }

    private Proposal getAcceptedProposal(Long projectId) {
        return proposalRepository.findAcceptedProposalByProjectId(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPOSAL_NOT_FOUND));
    }

    private void validateAssignedFreelancer(Long currentUserId, boolean adminOverride, Proposal acceptedProposal) {
        if (adminOverride) {
            return;
        }

        Long assignedUserId = acceptedProposal.getFreelancerProfile().getUser().getId();
        if (!assignedUserId.equals(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    private void validateRequestedStatus(Project project) {
        if (!project.isRequestedStatus()) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_STATUS);
        }
    }

    private void validateTimeRange(LocalDateTime requestedStartAt, LocalDateTime requestedEndAt) {
        if (!requestedEndAt.isAfter(requestedStartAt)) {
            throw new CustomException(ErrorCode.PROJECT_INVALID_TIME_RANGE);
        }
    }

    private void validateProjectCodes(String projectTypeCode, String serviceRegionCode) {
        codeValidationService.validateProjectTypeCode(projectTypeCode, "projectTypeCode");
        codeValidationService.validateRegionCode(serviceRegionCode, "serviceRegionCode");
    }
}
