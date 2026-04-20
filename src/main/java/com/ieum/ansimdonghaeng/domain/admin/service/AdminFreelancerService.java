package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminFreelancerActiveRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminFreelancerVisibilityRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminFreelancerStateResponse;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminPageQuerySupport;
import com.ieum.ansimdonghaeng.domain.file.support.FileKeySupport;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFreelancerService {

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final VerificationRepository verificationRepository;
    private final VerificationFileRepository verificationFileRepository;
    private final EntityManager entityManager;

    public PageResponse<AdminFreelancerListItemResponse> getFreelancers(Boolean verified,
                                                                        String keyword,
                                                                        String region,
                                                                        String projectType,
                                                                        Pageable pageable) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder selectBuilder = new StringBuilder("""
                select new com.ieum.ansimdonghaeng.domain.admin.service.AdminFreelancerService$FreelancerListRow(
                    profile.id,
                    user.id,
                    user.name,
                    user.email,
                    profile.verifiedYn,
                    profile.publicYn,
                    user.activeYn,
                    profile.averageRating,
                    profile.activityCount,
                    profile.createdAt,
                    profile.updatedAt
                )
                from FreelancerProfile profile
                join profile.user user
                where 1 = 1
                """);
        StringBuilder countBuilder = new StringBuilder("""
                select count(profile.id)
                from FreelancerProfile profile
                join profile.user user
                where 1 = 1
                """);

        if (verified != null) {
            appendCondition(selectBuilder, countBuilder, " and profile.verifiedYn = :verified");
            parameters.put("verified", verified);
        }
        if (StringUtils.hasText(keyword)) {
            appendCondition(selectBuilder, countBuilder, """
                     and (
                        lower(user.name) like :keyword
                        or lower(user.email) like :keyword
                     )
                    """);
            parameters.put("keyword", "%" + keyword.toLowerCase() + "%");
        }
        if (StringUtils.hasText(region)) {
            appendCondition(selectBuilder, countBuilder, """
                     and exists (
                        select 1
                        from FreelancerProfile regionProfile
                        join regionProfile.activityRegionCodes regionCode
                        where regionProfile = profile
                          and regionCode = :region
                     )
                    """);
            parameters.put("region", region);
        }
        if (StringUtils.hasText(projectType)) {
            appendCondition(selectBuilder, countBuilder, """
                     and exists (
                        select 1
                        from FreelancerProfile typeProfile
                        join typeProfile.projectTypeCodes projectTypeCode
                        where typeProfile = profile
                          and projectTypeCode = :projectType
                     )
                    """);
            parameters.put("projectType", projectType);
        }

        selectBuilder.append(" order by ")
                .append(AdminPageQuerySupport.orderByClause(
                        pageable,
                        Map.of(
                                "freelancerProfileId", "profile.id",
                                "createdAt", "profile.createdAt",
                                "updatedAt", "profile.updatedAt",
                                "averageRating", "profile.averageRating",
                                "activityCount", "profile.activityCount",
                                "name", "user.name"
                        ),
                        "profile.createdAt desc, profile.id desc"
                ));

        TypedQuery<FreelancerListRow> query = entityManager.createQuery(selectBuilder.toString(), FreelancerListRow.class);
        bindParameters(query, parameters);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AdminFreelancerListItemResponse> content = query.getResultList().stream()
                .map(row -> new AdminFreelancerListItemResponse(
                        row.freelancerProfileId(),
                        row.userId(),
                        row.name(),
                        row.email(),
                        row.verifiedYn(),
                        row.publicYn(),
                        row.activeYn(),
                        row.averageRating(),
                        row.activityCount(),
                        row.createdAt(),
                        row.updatedAt()
                ))
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countBuilder.toString(), Long.class);
        bindParameters(countQuery, parameters);
        long totalElements = countQuery.getSingleResult();

        return AdminPageQuerySupport.toPageResponse(content, pageable, totalElements);
    }

    public AdminFreelancerDetailResponse getFreelancer(Long freelancerProfileId) {
        FreelancerProfile profile = freelancerProfileRepository.findDetailById(freelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));
        List<Verification> recentVerifications = verificationRepository.findRecentByFreelancerProfileId(freelancerProfileId);

        List<AdminFreelancerDetailResponse.RecentVerificationResponse> verificationResponses = recentVerifications.stream()
                .limit(3)
                .map(verification -> new AdminFreelancerDetailResponse.RecentVerificationResponse(
                        verification.getId(),
                        verification.getVerificationType(),
                        verification.getStatus(),
                        verification.getRequestedAt(),
                        verification.getReviewedAt(),
                        verification.getRejectReason()
                ))
                .toList();

        List<AdminFreelancerDetailResponse.PortfolioFileResponse> portfolioFiles = recentVerifications.stream()
                .map(Verification::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .flatMap(verificationId -> verificationFileRepository.findAllByVerification_IdOrderByUploadedAtAsc(verificationId)
                        .stream())
                .limit(5)
                .map(file -> {
                    String fileKey = FileKeySupport.verificationKey(file.getId());
                    return new AdminFreelancerDetailResponse.PortfolioFileResponse(
                            file.getId(),
                            file.getOriginalName(),
                            file.getContentType(),
                            file.getFileSize(),
                            FileKeySupport.viewUrl(fileKey),
                            FileKeySupport.downloadUrl(fileKey),
                            file.getUploadedAt()
                    );
                })
                .toList();

        return new AdminFreelancerDetailResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getUser().getRoleCode(),
                profile.getUser().getActiveYn(),
                profile.getUser().getIntro(),
                profile.getCareerDescription(),
                profile.getCaregiverYn(),
                profile.getVerifiedYn(),
                profile.getPublicYn(),
                profile.getAverageRating(),
                profile.getActivityCount(),
                sorted(profile.getActivityRegionCodes()),
                sorted(profile.getAvailableTimeSlotCodes()),
                sorted(profile.getProjectTypeCodes()),
                verificationResponses,
                portfolioFiles,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    @Transactional
    public AdminFreelancerStateResponse updateVisibility(Long freelancerProfileId,
                                                         AdminFreelancerVisibilityRequest request) {
        FreelancerProfile profile = freelancerProfileRepository.findDetailById(freelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));
        profile.updatePublicYn(Boolean.TRUE.equals(request.publicYn()));
        return new AdminFreelancerStateResponse(
                profile.getId(),
                profile.getPublicYn(),
                profile.getUser().getActiveYn(),
                profile.getVerifiedYn()
        );
    }

    @Transactional
    public AdminFreelancerStateResponse updateActive(Long freelancerProfileId,
                                                     AdminFreelancerActiveRequest request) {
        FreelancerProfile profile = freelancerProfileRepository.findDetailById(freelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));
        profile.getUser().updateActive(Boolean.TRUE.equals(request.activeYn()));
        return new AdminFreelancerStateResponse(
                profile.getId(),
                profile.getPublicYn(),
                profile.getUser().getActiveYn(),
                profile.getVerifiedYn()
        );
    }

    private List<String> sorted(java.util.Set<String> values) {
        return values.stream().sorted(Comparator.naturalOrder()).toList();
    }

    private void appendCondition(StringBuilder selectBuilder, StringBuilder countBuilder, String condition) {
        selectBuilder.append(condition);
        countBuilder.append(condition);
    }

    private void bindParameters(jakarta.persistence.Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private record FreelancerListRow(
            Long freelancerProfileId,
            Long userId,
            String name,
            String email,
            Boolean verifiedYn,
            Boolean publicYn,
            Boolean activeYn,
            java.math.BigDecimal averageRating,
            Long activityCount,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
}
