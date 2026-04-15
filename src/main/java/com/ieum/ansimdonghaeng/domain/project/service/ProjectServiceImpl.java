package com.ieum.ansimdonghaeng.domain.project.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
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

    // 프로젝트 생성 시 기본 상태와 시간 범위를 함께 검증한다.
    @Override
    @Transactional
    public ProjectCreateResponse createProject(Long currentUserId, ProjectCreateRequest request) {
        validateTimeRange(request.requestedStartAt(), request.requestedEndAt());

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

    // 목록 조회는 owner 기준과 createdAt 내림차순 정렬을 기본으로 사용한다.
    @Override
    public ProjectListResponse getMyProjects(Long currentUserId, ProjectStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projectPage = projectRepository.findMyProjects(currentUserId, status, pageable);
        return ProjectListResponse.from(projectPage);
    }

    // 상세 조회는 존재 여부와 소유권을 분리해 404/403을 정확히 반환한다.
    @Override
    public ProjectDetailResponse getProject(Long currentUserId, Long projectId) {
        return ProjectDetailResponse.from(getOwnedProject(projectId, currentUserId));
    }

    // 부분 수정은 전달값만 반영하되 최종 시간 범위를 다시 검증한다.
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

    // 취소는 REQUESTED 상태에서만 가능하며 취소 사유와 시각을 함께 저장한다.
    @Override
    @Transactional
    public ProjectCancelResponse cancelProject(Long currentUserId, Long projectId, ProjectCancelRequest request) {
        Project project = getOwnedProject(projectId, currentUserId);
        validateRequestedStatus(project);

        project.cancel(request.reason(), LocalDateTime.now());
        return ProjectCancelResponse.from(project);
    }

    private Project getOwnedProject(Long projectId, Long currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return project;
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
}
