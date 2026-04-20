package com.ieum.ansimdonghaeng.domain.admin.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.response.PageResponse;
import com.ieum.ansimdonghaeng.domain.file.support.FileKeySupport;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminVerificationApproveRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.request.AdminVerificationRejectRequest;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminVerificationDetailResponse;
import com.ieum.ansimdonghaeng.domain.admin.dto.response.AdminVerificationListItemResponse;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminPageQuerySupport;
import com.ieum.ansimdonghaeng.domain.admin.support.AdminResponseMapper;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.notification.entity.Notification;
import com.ieum.ansimdonghaeng.domain.notification.entity.NotificationType;
import com.ieum.ansimdonghaeng.domain.notification.repository.NotificationRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationType;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class AdminVerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationFileRepository verificationFileRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EntityManager entityManager;

    public PageResponse<AdminVerificationListItemResponse> getVerifications(VerificationStatus status,
                                                                            VerificationType verificationType,
                                                                            String keyword,
                                                                            Pageable pageable) {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder selectBuilder = new StringBuilder("""
                select new com.ieum.ansimdonghaeng.domain.admin.service.AdminVerificationService$VerificationListRow(
                    verification.id,
                    freelancerProfile.id,
                    applicant.id,
                    applicant.name,
                    applicant.email,
                    verification.verificationType,
                    verification.status,
                    verification.requestedAt,
                    verification.reviewedAt
                )
                from Verification verification
                join verification.freelancerProfile freelancerProfile
                join freelancerProfile.user applicant
                where 1 = 1
                """);
        StringBuilder countBuilder = new StringBuilder("""
                select count(verification.id)
                from Verification verification
                join verification.freelancerProfile freelancerProfile
                join freelancerProfile.user applicant
                where 1 = 1
                """);

        if (status != null) {
            appendCondition(selectBuilder, countBuilder, " and verification.status = :status");
            parameters.put("status", status);
        }
        if (verificationType != null) {
            appendCondition(selectBuilder, countBuilder, " and verification.verificationType = :verificationType");
            parameters.put("verificationType", verificationType);
        }
        if (StringUtils.hasText(keyword)) {
            appendCondition(selectBuilder, countBuilder, """
                     and (
                        lower(applicant.name) like :keyword
                        or lower(applicant.email) like :keyword
                        or str(verification.id) like :idKeyword
                     )
                    """);
            parameters.put("keyword", "%" + keyword.toLowerCase() + "%");
            parameters.put("idKeyword", "%" + keyword + "%");
        }

        selectBuilder.append(" order by ")
                .append(AdminPageQuerySupport.orderByClause(
                        pageable,
                        Map.of(
                                "verificationId", "verification.id",
                                "requestedAt", "verification.requestedAt",
                                "reviewedAt", "verification.reviewedAt",
                                "status", "verification.status"
                        ),
                        "verification.requestedAt desc, verification.id desc"
                ));

        TypedQuery<VerificationListRow> query = entityManager.createQuery(selectBuilder.toString(), VerificationListRow.class);
        bindParameters(query, parameters);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<VerificationListRow> rows = query.getResultList();
        List<Long> verificationIds = rows.stream().map(VerificationListRow::verificationId).toList();
        Map<Long, String> descriptionSummaries = verificationRepository.findAllById(verificationIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Verification::getId,
                        verification -> summarize(verification.getDescription())
                ));
        Set<Long> verificationIdsWithFiles = new HashSet<>(
                verificationIds.isEmpty() ? List.of() : verificationFileRepository.findVerificationIdsWithFiles(verificationIds)
        );

        List<AdminVerificationListItemResponse> content = rows.stream()
                .map(row -> new AdminVerificationListItemResponse(
                        row.verificationId(),
                        row.freelancerProfileId(),
                        row.userId(),
                        row.applicantName(),
                        row.applicantEmail(),
                        row.verificationType(),
                        row.status(),
                        descriptionSummaries.get(row.verificationId()),
                        verificationIdsWithFiles.contains(row.verificationId()),
                        row.requestedAt(),
                        row.reviewedAt()
                ))
                .toList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countBuilder.toString(), Long.class);
        bindParameters(countQuery, parameters);
        long totalElements = countQuery.getSingleResult();

        return AdminPageQuerySupport.toPageResponse(content, pageable, totalElements);
    }

    public AdminVerificationDetailResponse getVerification(Long verificationId) {
        Verification verification = verificationRepository.findDetailById(verificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));
        return toDetailResponse(verification);
    }

    @Transactional
    public AdminVerificationDetailResponse approve(Long adminUserId,
                                                   Long verificationId,
                                                   AdminVerificationApproveRequest request) {
        Verification verification = verificationRepository.findDetailByIdForUpdate(verificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));
        if (!verification.isPending()) {
            throw new CustomException(ErrorCode.VERIFICATION_ALREADY_REVIEWED);
        }

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Admin user was not found."));
        verification.approve(adminUser, java.time.LocalDateTime.now());
        verification.getFreelancerProfile().updateVerifiedYn(true);
        notificationRepository.save(Notification.create(
                verification.getFreelancerProfile().getUser(),
                NotificationType.VERIFICATION_APPROVED,
                "검증 요청이 승인되었습니다.",
                verification.getVerificationType().name(),
                null,
                null,
                null,
                null,
                verification.getId()
        ));
        return toDetailResponse(verification);
    }

    @Transactional
    public AdminVerificationDetailResponse reject(Long adminUserId,
                                                  Long verificationId,
                                                  AdminVerificationRejectRequest request) {
        Verification verification = verificationRepository.findDetailByIdForUpdate(verificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));
        if (!verification.isPending()) {
            throw new CustomException(ErrorCode.VERIFICATION_ALREADY_REVIEWED);
        }

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "Admin user was not found."));
        verification.reject(adminUser, request.reviewComment(), java.time.LocalDateTime.now());
        verificationRepository.flush();
        FreelancerProfile profile = verification.getFreelancerProfile();
        boolean hasApprovedVerification = verificationRepository.existsByFreelancerProfile_IdAndStatus(
                profile.getId(),
                VerificationStatus.APPROVED
        );
        profile.updateVerifiedYn(hasApprovedVerification);
        notificationRepository.save(Notification.create(
                verification.getFreelancerProfile().getUser(),
                NotificationType.VERIFICATION_REJECTED,
                "검증 요청이 반려되었습니다.",
                request.reviewComment(),
                null,
                null,
                null,
                null,
                verification.getId()
        ));
        return toDetailResponse(verification);
    }

    private AdminVerificationDetailResponse toDetailResponse(Verification verification) {
        List<AdminVerificationDetailResponse.FileSummaryResponse> files =
                verificationFileRepository.findAllByVerification_IdOrderByUploadedAtAsc(verification.getId())
                        .stream()
                        .map(this::toFileSummaryResponse)
                        .toList();

        return new AdminVerificationDetailResponse(
                verification.getId(),
                verification.getFreelancerProfile().getId(),
                AdminResponseMapper.toFreelancerSummary(verification.getFreelancerProfile()),
                verification.getVerificationType(),
                verification.getStatus(),
                verification.getDescription(),
                verification.getRequestedAt(),
                verification.getReviewedAt(),
                AdminResponseMapper.toUserSummary(verification.getReviewedByUser()),
                verification.getRejectReason(),
                files
        );
    }

    private AdminVerificationDetailResponse.FileSummaryResponse toFileSummaryResponse(VerificationFile file) {
        String fileKey = FileKeySupport.verificationKey(file.getId());
        return new AdminVerificationDetailResponse.FileSummaryResponse(
                file.getId(),
                file.getOriginalName(),
                file.getStoredName(),
                file.getContentType(),
                file.getFileSize(),
                FileKeySupport.viewUrl(fileKey),
                FileKeySupport.downloadUrl(fileKey),
                file.getUploadedAt()
        );
    }

    private void appendCondition(StringBuilder selectBuilder, StringBuilder countBuilder, String condition) {
        selectBuilder.append(condition);
        countBuilder.append(condition);
    }

    private void bindParameters(jakarta.persistence.Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private String summarize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String normalized = value.trim();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120);
    }

    private record VerificationListRow(
            Long verificationId,
            Long freelancerProfileId,
            Long userId,
            String applicantName,
            String applicantEmail,
            VerificationType verificationType,
            VerificationStatus status,
            LocalDateTime requestedAt,
            LocalDateTime reviewedAt
    ) {
    }
}
