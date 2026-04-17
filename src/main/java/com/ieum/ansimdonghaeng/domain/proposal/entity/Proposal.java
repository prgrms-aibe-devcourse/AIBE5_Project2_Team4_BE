package com.ieum.ansimdonghaeng.domain.proposal.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PROPOSAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "proposal_seq_generator", sequenceName = "SEQ_PROPOSAL", allocationSize = 1)
public class Proposal extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "proposal_seq_generator")
    @Column(name = "PROPOSAL_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FREELANCER_PROFILE_ID", nullable = false)
    private FreelancerProfile freelancerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_CODE", nullable = false, length = 20)
    private ProposalStatus status;

    @Column(name = "MESSAGE", length = 2000)
    private String message;

    @Column(name = "RESPONDED_AT")
    private LocalDateTime respondedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Proposal(Project project,
                     FreelancerProfile freelancerProfile,
                     ProposalStatus status,
                     String message,
                     LocalDateTime respondedAt) {
        this.project = project;
        this.freelancerProfile = freelancerProfile;
        this.status = status;
        this.message = message;
        this.respondedAt = respondedAt;
    }

    // 사용자 제안은 최초에 항상 PENDING 상태로 생성한다.
    public static Proposal create(Project project, FreelancerProfile freelancerProfile, String message) {
        return Proposal.builder()
                .project(project)
                .freelancerProfile(freelancerProfile)
                .status(ProposalStatus.PENDING)
                .message(message)
                .build();
    }

    public boolean isPendingStatus() {
        return status == ProposalStatus.PENDING;
    }

    public boolean isOwnedByFreelancerProfile(Long freelancerProfileId) {
        return freelancerProfile.getId().equals(freelancerProfileId);
    }

    public boolean isAcceptedStatus() {
        return status == ProposalStatus.ACCEPTED;
    }

    // 프리랜서가 제안을 수락하면 상태와 응답 시각을 함께 저장한다.
    public void accept(LocalDateTime respondedAt) {
        this.status = ProposalStatus.ACCEPTED;
        this.respondedAt = respondedAt;
    }

    // 다른 제안은 수락 연동 시점에 자동 거절해 중복 수락을 막는다.
    public void reject(LocalDateTime respondedAt) {
        this.status = ProposalStatus.REJECTED;
        this.respondedAt = respondedAt;
    }
}
