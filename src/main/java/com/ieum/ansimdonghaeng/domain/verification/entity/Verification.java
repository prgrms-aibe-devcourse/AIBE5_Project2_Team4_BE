package com.ieum.ansimdonghaeng.domain.verification.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VERIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "verification_seq_generator", sequenceName = "SEQ_VERIFICATION", allocationSize = 1)
public class Verification extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verification_seq_generator")
    @Column(name = "VERIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FREELANCER_PROFILE_ID", nullable = false)
    private FreelancerProfile freelancerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "VERIFICATION_TYPE_CODE", nullable = false, length = 60)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_CODE", nullable = false, length = 40)
    private VerificationStatus status;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEWED_BY_USER_ID")
    private User reviewedByUser;

    @Column(name = "REQUESTED_AT", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "REVIEWED_AT")
    private LocalDateTime reviewedAt;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "REJECT_REASON")
    private String rejectReason;

    @Builder(access = AccessLevel.PRIVATE)
    private Verification(FreelancerProfile freelancerProfile,
                         VerificationType verificationType,
                         VerificationStatus status,
                         String description,
                         User reviewedByUser,
                         LocalDateTime requestedAt,
                         LocalDateTime reviewedAt,
                         String rejectReason) {
        this.freelancerProfile = freelancerProfile;
        this.verificationType = verificationType;
        this.status = status;
        this.description = description;
        this.reviewedByUser = reviewedByUser;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
        this.rejectReason = rejectReason;
    }

    public static Verification create(FreelancerProfile freelancerProfile,
                                      VerificationType verificationType,
                                      String description,
                                      LocalDateTime requestedAt) {
        return Verification.builder()
                .freelancerProfile(freelancerProfile)
                .verificationType(verificationType)
                .status(VerificationStatus.PENDING)
                .description(description)
                .requestedAt(requestedAt)
                .build();
    }

    public boolean isPending() {
        return status == VerificationStatus.PENDING;
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(freelancerProfile.getUser().getId(), userId);
    }

    public void approve(User adminUser, LocalDateTime reviewedAt) {
        this.status = VerificationStatus.APPROVED;
        this.reviewedByUser = adminUser;
        this.reviewedAt = reviewedAt;
        this.rejectReason = null;
    }

    public void reject(User adminUser, String rejectReason, LocalDateTime reviewedAt) {
        this.status = VerificationStatus.REJECTED;
        this.reviewedByUser = adminUser;
        this.reviewedAt = reviewedAt;
        this.rejectReason = rejectReason;
    }
}
