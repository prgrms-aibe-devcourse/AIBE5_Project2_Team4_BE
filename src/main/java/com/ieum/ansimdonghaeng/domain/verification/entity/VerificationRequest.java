package com.ieum.ansimdonghaeng.domain.verification.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "VERIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "verification_seq_generator", sequenceName = "SEQ_VERIFICATION", allocationSize = 1)
public class VerificationRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verification_seq_generator")
    @Column(name = "VERIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FREELANCER_PROFILE_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FreelancerProfile freelancerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "VERIFICATION_TYPE_CODE", nullable = false, length = 60)
    private VerificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_CODE", nullable = false, length = 40)
    private VerificationStatus status;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DESCRIPTION")
    private String requestMessage;

    @Column(name = "REVIEWED_BY_USER_ID")
    private Long reviewedByUserId;

    @Column(name = "REQUESTED_AT", nullable = false)
    private LocalDateTime requestedAt;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "REJECT_REASON")
    private String rejectReason;

    @Column(name = "REVIEWED_AT")
    private LocalDateTime reviewedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private VerificationRequest(FreelancerProfile freelancerProfile,
                                VerificationType type,
                                VerificationStatus status,
                                String requestMessage,
                                Long reviewedByUserId,
                                LocalDateTime requestedAt,
                                String rejectReason,
                                LocalDateTime reviewedAt) {
        this.freelancerProfile = freelancerProfile;
        this.type = type;
        this.status = status;
        this.requestMessage = requestMessage;
        this.reviewedByUserId = reviewedByUserId;
        this.requestedAt = requestedAt;
        this.rejectReason = rejectReason;
        this.reviewedAt = reviewedAt;
    }

    public static VerificationRequest create(FreelancerProfile freelancerProfile,
                                             VerificationType type,
                                             String requestMessage) {
        return VerificationRequest.builder()
                .freelancerProfile(freelancerProfile)
                .type(type)
                .status(VerificationStatus.PENDING)
                .requestMessage(requestMessage)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(freelancerProfile.getUser().getId(), userId);
    }

    public void approve(Long reviewedByUserId, LocalDateTime reviewedAt) {
        this.status = VerificationStatus.APPROVED;
        this.reviewedByUserId = reviewedByUserId;
        this.rejectReason = null;
        this.reviewedAt = reviewedAt;
        this.freelancerProfile.markVerified();
    }

    public void reject(Long reviewedByUserId, String rejectReason, LocalDateTime reviewedAt) {
        this.status = VerificationStatus.REJECTED;
        this.reviewedByUserId = reviewedByUserId;
        this.rejectReason = rejectReason;
        this.reviewedAt = reviewedAt;
    }
}
