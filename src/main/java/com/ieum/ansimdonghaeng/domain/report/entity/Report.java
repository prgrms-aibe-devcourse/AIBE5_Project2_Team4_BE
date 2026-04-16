package com.ieum.ansimdonghaeng.domain.report.entity;

import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Basic;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "REPORT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "report_seq_generator", sequenceName = "SEQ_REPORT", allocationSize = 1)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_generator")
    @Column(name = "REPORT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REPORTER_USER_ID", nullable = false)
    private User reporterUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "REASON_TYPE_CODE", nullable = false, length = 60)
    private ReportReasonType reasonType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "REASON_DETAIL")
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_CODE", nullable = false, length = 40)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HANDLED_BY_USER_ID")
    private User handledByUser;

    @Column(name = "HANDLED_AT")
    private LocalDateTime handledAt;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Report(Review review,
                   User reporterUser,
                   ReportReasonType reasonType,
                   String reasonDetail,
                   ReportStatus status,
                   User handledByUser,
                   LocalDateTime handledAt) {
        this.review = review;
        this.reporterUser = reporterUser;
        this.reasonType = reasonType;
        this.reasonDetail = reasonDetail;
        this.status = status;
        this.handledByUser = handledByUser;
        this.handledAt = handledAt;
    }

    public static Report create(Review review, User reporterUser, ReportReasonType reasonType, String reasonDetail) {
        return Report.builder()
                .review(review)
                .reporterUser(reporterUser)
                .reasonType(reasonType)
                .reasonDetail(reasonDetail)
                .status(ReportStatus.PENDING)
                .build();
    }

    public boolean isHandled() {
        return status != ReportStatus.PENDING;
    }

    public void resolve(User adminUser, LocalDateTime handledAt) {
        this.status = ReportStatus.RESOLVED;
        this.handledByUser = adminUser;
        this.handledAt = handledAt;
    }

    public void reject(User adminUser, LocalDateTime handledAt) {
        this.status = ReportStatus.REJECTED;
        this.handledByUser = adminUser;
        this.handledAt = handledAt;
    }
}
