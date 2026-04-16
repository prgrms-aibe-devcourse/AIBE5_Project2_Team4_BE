package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminReviewBlindRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReviewListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminUserSummaryResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminReviewVisibilityResponse;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminPageQuerySupport;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminResponseMapper;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.proposal.repository.ProposalRepository;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
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
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ProposalRepository proposalRepository;
    private final EntityManager entityManager;

    public PageResponse<AdminReviewListItemResponse> getReviews(Boolean blinded, String keyword, Pageable pageable) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder selectBuilder = new StringBuilder("""
                select new com.ieum.ansimdonghaeng.domain.admin.service.AdminReviewService$ReviewListRow(
                    review.id,
                    project.id,
                    project.title,
                    owner.id,
                    owner.name,
                    owner.email,
                    owner.roleCode,
                    owner.activeYn,
                    review.rating,
                    review.blindedYn,
                    review.createdAt
                )
                from Review review
                join review.project project
                join project.ownerUser owner
                where 1 = 1
                """);
        StringBuilder countBuilder = new StringBuilder("""
                select count(review.id)
                from Review review
                join review.project project
                join project.ownerUser owner
                where 1 = 1
                """);

        if (blinded != null) {
            appendCondition(selectBuilder, countBuilder, " and review.blindedYn = :blindedYn");
            parameters.put("blindedYn", blinded ? "Y" : "N");
        }
        if (StringUtils.hasText(keyword)) {
            appendCondition(selectBuilder, countBuilder, """
                     and (
                        lower(project.title) like :keyword
                        or lower(owner.name) like :keyword
                        or lower(owner.email) like :keyword
                     )
                    """);
            parameters.put("keyword", "%" + keyword.toLowerCase() + "%");
        }

        selectBuilder.append(" order by ")
                .append(AdminPageQuerySupport.orderByClause(
                        pageable,
                        Map.of(
                                "reviewId", "review.id",
                                "createdAt", "review.createdAt",
                                "rating", "review.rating",
                                "projectId", "project.id"
                        ),
                        "review.createdAt desc, review.id desc"
                ));

        TypedQuery<ReviewListRow> query = entityManager.createQuery(selectBuilder.toString(), ReviewListRow.class);
        bindParameters(query, parameters);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ReviewListRow> rows = query.getResultList();
        Map<Long, Proposal> acceptedProposalByProjectId = proposalRepository.findAcceptedProposalsByProjectIds(
                        rows.stream().map(ReviewListRow::projectId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        proposal -> proposal.getProject().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<AdminReviewListItemResponse> content = rows.stream()
                .map(row -> {
                    Proposal acceptedProposal = acceptedProposalByProjectId.get(row.projectId());
                    return new AdminReviewListItemResponse(
                            row.reviewId(),
                            row.projectId(),
                            row.projectTitle(),
                            row.writer(),
                            acceptedProposal != null ? AdminResponseMapper.toFreelancerSummary(acceptedProposal.getFreelancerProfile()) : null,
                            row.rating(),
                            row.blinded(),
                            row.createdAt()
                    );
                })
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countBuilder.toString(), Long.class);
        bindParameters(countQuery, parameters);
        long totalElements = countQuery.getSingleResult();

        return AdminPageQuerySupport.toPageResponse(content, pageable, totalElements);
    }

    @Transactional
    public AdminReviewVisibilityResponse blind(Long reviewId, AdminReviewBlindRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        review.blind();
        return new AdminReviewVisibilityResponse(review.getId(), true);
    }

    @Transactional
    public AdminReviewVisibilityResponse unblind(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        review.unblind();
        return new AdminReviewVisibilityResponse(review.getId(), false);
    }

    private void appendCondition(StringBuilder selectBuilder, StringBuilder countBuilder, String condition) {
        selectBuilder.append(condition);
        countBuilder.append(condition);
    }

    private void bindParameters(jakarta.persistence.Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private record ReviewListRow(
            Long reviewId,
            Long projectId,
            String projectTitle,
            Long writerUserId,
            String writerName,
            String writerEmail,
            String writerRoleCode,
            Boolean writerActiveYn,
            Integer rating,
            String blindedYn,
            LocalDateTime createdAt
    ) {
        private AdminUserSummaryResponse writer() {
            return new AdminUserSummaryResponse(
                    writerUserId,
                    writerName,
                    writerEmail,
                    writerRoleCode,
                    writerActiveYn
            );
        }

        private boolean blinded() {
            return "Y".equalsIgnoreCase(blindedYn);
        }
    }
}
