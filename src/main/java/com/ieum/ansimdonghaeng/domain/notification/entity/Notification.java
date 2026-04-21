package com.ieum.ansimdonghaeng.domain.notification.entity;

import com.ieum.ansimdonghaeng.domain.notice.entity.Notice;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.proposal.entity.Proposal;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "notification_seq_generator", sequenceName = "SEQ_NOTIFICATION", allocationSize = 1)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq_generator")
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "NOTIFICATION_TYPE_CODE", nullable = false, length = 100)
    private NotificationType notificationType;

    @Column(name = "TITLE", nullable = false, length = 400)
    private String title;

    @Column(name = "CONTENT", nullable = false, length = 2000)
    private String content;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "READ_YN", nullable = false, length = 1)
    private Boolean readYn;

    @Column(name = "RELATED_PROJECT_ID")
    private Long relatedProjectId;

    @Column(name = "RELATED_PROPOSAL_ID")
    private Long relatedProposalId;

    @Column(name = "RELATED_REVIEW_ID")
    private Long relatedReviewId;

    @Column(name = "RELATED_NOTICE_ID")
    private Long relatedNoticeId;

    @Column(name = "RELATED_VERIFICATION_ID")
    private Long relatedVerificationId;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "READ_AT")
    private LocalDateTime readAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Notification(User user,
                         NotificationType notificationType,
                         String title,
                         String content,
                         Boolean readYn,
                         Long relatedProjectId,
                         Long relatedProposalId,
                         Long relatedReviewId,
                         Long relatedNoticeId,
                         Long relatedVerificationId,
                         LocalDateTime readAt) {
        this.user = user;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.readYn = readYn;
        this.relatedProjectId = relatedProjectId;
        this.relatedProposalId = relatedProposalId;
        this.relatedReviewId = relatedReviewId;
        this.relatedNoticeId = relatedNoticeId;
        this.relatedVerificationId = relatedVerificationId;
        this.readAt = readAt;
    }

    public static Notification create(User user,
                                      NotificationType notificationType,
                                      String title,
                                      String content,
                                      Long relatedProjectId,
                                      Long relatedProposalId,
                                      Long relatedReviewId,
                                      Long relatedNoticeId,
                                      Long relatedVerificationId) {
        return Notification.builder()
                .user(user)
                .notificationType(notificationType)
                .title(title)
                .content(content)
                .readYn(false)
                .relatedProjectId(relatedProjectId)
                .relatedProposalId(relatedProposalId)
                .relatedReviewId(relatedReviewId)
                .relatedNoticeId(relatedNoticeId)
                .relatedVerificationId(relatedVerificationId)
                .build();
    }

    public static Notification proposalReceived(User user, Proposal proposal, String title, String content) {
        return create(
                user,
                NotificationType.PROPOSAL_RECEIVED,
                title,
                content,
                proposal.getProject().getId(),
                proposal.getId(),
                null,
                null,
                null
        );
    }

    public static Notification proposalAccepted(User user, Proposal proposal, String title, String content) {
        return create(
                user,
                NotificationType.PROPOSAL_ACCEPTED,
                title,
                content,
                proposal.getProject().getId(),
                proposal.getId(),
                null,
                null,
                null
        );
    }

    public static Notification projectStatusChanged(User user, Project project, String title, String content) {
        return create(
                user,
                NotificationType.PROJECT_STATUS_CHANGED,
                title,
                content,
                project.getId(),
                null,
                null,
                null,
                null
        );
    }

    public static Notification reviewRequest(User user, Project project, String title, String content) {
        return create(
                user,
                NotificationType.REVIEW_REQUEST,
                title,
                content,
                project.getId(),
                null,
                null,
                null,
                null
        );
    }

    public static Notification notice(User user, Notice notice, String content) {
        return create(
                user,
                NotificationType.NOTICE,
                notice.getTitle(),
                content,
                null,
                null,
                null,
                notice.getId(),
                null
        );
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(user.getId(), userId);
    }

    public void markRead(LocalDateTime readAt) {
        if (Boolean.TRUE.equals(readYn)) {
            return;
        }
        this.readYn = true;
        this.readAt = readAt;
    }
}
