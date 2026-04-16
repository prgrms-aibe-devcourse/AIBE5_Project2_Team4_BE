package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminProjectDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminProjectSummaryResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminUserSummaryResponse;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminPageQuerySupport;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminResponseMapper;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCancelRequest;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminProjectService {

    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final EntityManager entityManager;

    public PageResponse<AdminProjectSummaryResponse> getProjects(ProjectStatus status,
                                                                 String keyword,
                                                                 String writerKeyword,
                                                                 Pageable pageable) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder selectBuilder = new StringBuilder("""
                select new com.ieum.ansimdonghaeng.domain.admin.service.AdminProjectService$ProjectListRow(
                    project.id,
                    project.title,
                    project.projectTypeCode,
                    project.status,
                    owner.id,
                    owner.name,
                    owner.email,
                    owner.roleCode,
                    owner.activeYn,
                    project.requestedStartAt,
                    project.requestedEndAt,
                    project.createdAt,
                    project.updatedAt
                )
                from Project project
                join project.ownerUser owner
                where 1 = 1
                """);
        StringBuilder countBuilder = new StringBuilder("""
                select count(project.id)
                from Project project
                join project.ownerUser owner
                where 1 = 1
                """);

        if (status != null) {
            appendCondition(selectBuilder, countBuilder, " and project.status = :status");
            parameters.put("status", status);
        }
        if (StringUtils.hasText(keyword)) {
            appendCondition(selectBuilder, countBuilder, """
                     and (
                        lower(project.title) like :keyword
                        or str(project.id) like :idKeyword
                     )
                    """);
            parameters.put("keyword", "%" + keyword.toLowerCase() + "%");
            parameters.put("idKeyword", "%" + keyword + "%");
        }
        if (StringUtils.hasText(writerKeyword)) {
            appendCondition(selectBuilder, countBuilder, """
                     and (
                        lower(owner.name) like :writerKeyword
                        or lower(owner.email) like :writerKeyword
                     )
                    """);
            parameters.put("writerKeyword", "%" + writerKeyword.toLowerCase() + "%");
        }

        selectBuilder.append(" order by ")
                .append(AdminPageQuerySupport.orderByClause(
                        pageable,
                        Map.of(
                                "projectId", "project.id",
                                "createdAt", "project.createdAt",
                                "updatedAt", "project.updatedAt",
                                "requestedStartAt", "project.requestedStartAt",
                                "requestedEndAt", "project.requestedEndAt",
                                "status", "project.status",
                                "title", "project.title"
                        ),
                        "project.createdAt desc, project.id desc"
                ));

        TypedQuery<ProjectListRow> query = entityManager.createQuery(selectBuilder.toString(), ProjectListRow.class);
        bindParameters(query, parameters);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ProjectListRow> rows = query.getResultList();
        Map<Long, Proposal> acceptedProposalByProjectId = proposalRepository.findAcceptedProposalsByProjectIds(
                        rows.stream().map(ProjectListRow::projectId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        proposal -> proposal.getProject().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<AdminProjectSummaryResponse> content = rows.stream()
                .map(row -> toSummaryResponse(row, acceptedProposalByProjectId.get(row.projectId())))
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countBuilder.toString(), Long.class);
        bindParameters(countQuery, parameters);
        long totalElements = countQuery.getSingleResult();

        return AdminPageQuerySupport.toPageResponse(content, pageable, totalElements);
    }

    public AdminProjectDetailResponse getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        Proposal acceptedProposal = proposalRepository.findAcceptedProposalByProjectId(projectId).orElse(null);
        return toDetailResponse(project, acceptedProposal);
    }

    @Transactional
    public AdminProjectDetailResponse cancelProject(Long projectId, ProjectCancelRequest request) {
        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
        if (!project.canAdminCancel()) {
            throw new CustomException(ErrorCode.PROJECT_ADMIN_CANCEL_NOT_ALLOWED);
        }
        project.cancel(request.reason(), java.time.LocalDateTime.now());
        Proposal acceptedProposal = proposalRepository.findAcceptedProposalByProjectId(projectId).orElse(null);
        return toDetailResponse(project, acceptedProposal);
    }

    private AdminProjectSummaryResponse toSummaryResponse(ProjectListRow row, Proposal acceptedProposal) {
        return new AdminProjectSummaryResponse(
                row.projectId(),
                row.title(),
                row.projectTypeCode(),
                row.status(),
                row.owner(),
                acceptedProposal != null ? AdminResponseMapper.toFreelancerSummary(acceptedProposal.getFreelancerProfile()) : null,
                row.requestedStartAt(),
                row.requestedEndAt(),
                row.createdAt(),
                row.updatedAt()
        );
    }

    private AdminProjectDetailResponse toDetailResponse(Project project, Proposal acceptedProposal) {
        AdminProjectDetailResponse.AcceptedProposalResponse acceptedProposalResponse = acceptedProposal == null
                ? null
                : new AdminProjectDetailResponse.AcceptedProposalResponse(
                        acceptedProposal.getId(),
                        AdminResponseMapper.toFreelancerSummary(acceptedProposal.getFreelancerProfile()),
                        acceptedProposal.getMessage(),
                        acceptedProposal.getRespondedAt()
                );

        return new AdminProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getProjectTypeCode(),
                project.getServiceRegionCode(),
                project.getRequestedStartAt(),
                project.getRequestedEndAt(),
                project.getServiceAddress(),
                project.getServiceDetailAddress(),
                project.getRequestDetail(),
                project.getStatus(),
                AdminResponseMapper.toUserSummary(project.getOwnerUser()),
                acceptedProposalResponse,
                project.getAcceptedAt(),
                project.getStartedAt(),
                project.getCompletedAt(),
                project.getCancelledAt(),
                project.getCancelledReason(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private void appendCondition(StringBuilder selectBuilder, StringBuilder countBuilder, String condition) {
        selectBuilder.append(condition);
        countBuilder.append(condition);
    }

    private void bindParameters(jakarta.persistence.Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private record ProjectListRow(
            Long projectId,
            String title,
            String projectTypeCode,
            ProjectStatus status,
            Long ownerUserId,
            String ownerName,
            String ownerEmail,
            String ownerRoleCode,
            Boolean ownerActiveYn,
            LocalDateTime requestedStartAt,
            LocalDateTime requestedEndAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        private AdminUserSummaryResponse owner() {
            return new AdminUserSummaryResponse(
                    ownerUserId,
                    ownerName,
                    ownerEmail,
                    ownerRoleCode,
                    ownerActiveYn
            );
        }
    }
}
