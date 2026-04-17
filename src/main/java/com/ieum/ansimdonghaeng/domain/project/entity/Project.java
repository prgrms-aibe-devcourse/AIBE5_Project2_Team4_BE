package com.ieum.ansimdonghaeng.domain.project.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
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
import jakarta.persistence.ForeignKey;
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
@Table(name = "PROJECT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "project_seq_generator", sequenceName = "SEQ_PROJECT", allocationSize = 1)
public class Project extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq_generator")
    @Column(name = "PROJECT_ID")
    private Long id;

    @Column(name = "OWNER_USER_ID", nullable = false)
    private Long ownerUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_USER_ID", insertable = false, updatable = false, foreignKey = @ForeignKey(value = jakarta.persistence.ConstraintMode.NO_CONSTRAINT))
    private User ownerUser;

    @Column(name = "TITLE", nullable = false, length = 400)
    private String title;

    // TODO: 공통 코드 도메인이 정리되면 FK나 enum 연동을 검토한다.
    @Column(name = "PROJECT_TYPE_CODE", nullable = false, length = 60)
    private String projectTypeCode;

    // TODO: 공통 코드 도메인이 정리되면 FK나 enum 연동을 검토한다.
    @Column(name = "SERVICE_REGION_CODE", nullable = false, length = 40)
    private String serviceRegionCode;

    @Column(name = "REQUESTED_START_AT", nullable = false)
    private LocalDateTime requestedStartAt;

    @Column(name = "REQUESTED_END_AT", nullable = false)
    private LocalDateTime requestedEndAt;

    @Column(name = "SERVICE_ADDRESS", nullable = false, length = 600)
    private String serviceAddress;

    @Column(name = "SERVICE_DETAIL_ADDRESS", length = 600)
    private String serviceDetailAddress;

    // Oracle 스키마에서 요청 상세는 CLOB으로 관리한다.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "REQUEST_DETAIL", nullable = false)
    private String requestDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_CODE", nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "ACCEPTED_AT")
    private LocalDateTime acceptedAt;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    // Oracle 스키마에서 취소 사유도 CLOB으로 관리한다.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "CANCELLED_REASON")
    private String cancelledReason;

    @Builder(access = AccessLevel.PRIVATE)
    private Project(Long ownerUserId,
                    String title,
                    String projectTypeCode,
                    String serviceRegionCode,
                    LocalDateTime requestedStartAt,
                    LocalDateTime requestedEndAt,
                    String serviceAddress,
                    String serviceDetailAddress,
                    String requestDetail,
                    ProjectStatus status,
                    LocalDateTime acceptedAt,
                    LocalDateTime startedAt,
                    LocalDateTime completedAt,
                    LocalDateTime cancelledAt,
                    String cancelledReason) {
        this.ownerUserId = ownerUserId;
        this.title = title;
        this.projectTypeCode = projectTypeCode;
        this.serviceRegionCode = serviceRegionCode;
        this.requestedStartAt = requestedStartAt;
        this.requestedEndAt = requestedEndAt;
        this.serviceAddress = serviceAddress;
        this.serviceDetailAddress = serviceDetailAddress;
        this.requestDetail = requestDetail;
        this.status = status;
        this.acceptedAt = acceptedAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
    }

    // 프로젝트 생성 시점에는 항상 REQUESTED 상태로 시작한다.
    public static Project create(Long ownerUserId,
                                 String title,
                                 String projectTypeCode,
                                 String serviceRegionCode,
                                 LocalDateTime requestedStartAt,
                                 LocalDateTime requestedEndAt,
                                 String serviceAddress,
                                 String serviceDetailAddress,
                                 String requestDetail) {
        return Project.builder()
                .ownerUserId(ownerUserId)
                .title(title)
                .projectTypeCode(projectTypeCode)
                .serviceRegionCode(serviceRegionCode)
                .requestedStartAt(requestedStartAt)
                .requestedEndAt(requestedEndAt)
                .serviceAddress(serviceAddress)
                .serviceDetailAddress(serviceDetailAddress)
                .requestDetail(requestDetail)
                .status(ProjectStatus.REQUESTED)
                .build();
    }

    // ownerUserId scalar로 현재 로그인 사용자의 소유권을 비교한다.
    public boolean isOwnedBy(Long currentUserId) {
        return Objects.equals(ownerUserId, currentUserId);
    }

    public boolean isRequestedStatus() {
        return status == ProjectStatus.REQUESTED;
    }

    public boolean isAcceptedStatus() {
        return status == ProjectStatus.ACCEPTED;
    }

    public boolean isInProgressStatus() {
        return status == ProjectStatus.IN_PROGRESS;
    }

    public boolean canAdminCancel() {
        return status != ProjectStatus.COMPLETED && status != ProjectStatus.CANCELLED;
    }

    // 수정은 서비스에서 검증을 끝낸 뒤 최종 반영값만 엔티티에 반영한다.
    public void update(String title,
                       String projectTypeCode,
                       String serviceRegionCode,
                       LocalDateTime requestedStartAt,
                       LocalDateTime requestedEndAt,
                       String serviceAddress,
                       String serviceDetailAddress,
                       String requestDetail) {
        this.title = title;
        this.projectTypeCode = projectTypeCode;
        this.serviceRegionCode = serviceRegionCode;
        this.requestedStartAt = requestedStartAt;
        this.requestedEndAt = requestedEndAt;
        this.serviceAddress = serviceAddress;
        this.serviceDetailAddress = serviceDetailAddress;
        this.requestDetail = requestDetail;
    }

    // 제안이 수락되면 프로젝트 상태를 ACCEPTED로 바꾸고 수락 시각을 남긴다.
    public void accept(LocalDateTime acceptedAt) {
        this.status = ProjectStatus.ACCEPTED;
        this.acceptedAt = acceptedAt;
    }

    // 서비스 시작 시점에는 진행 중 상태와 시작 시각을 함께 남긴다.
    public void start(LocalDateTime startedAt) {
        this.status = ProjectStatus.IN_PROGRESS;
        this.startedAt = startedAt;
    }

    // 서비스 완료 시점에는 완료 상태와 완료 시각을 함께 남긴다.
    public void complete(LocalDateTime completedAt) {
        this.status = ProjectStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    // 취소 시점과 취소 사유를 함께 저장해 이후 상태 추적이 가능하도록 한다.
    public void cancel(String reason, LocalDateTime cancelledAt) {
        this.status = ProjectStatus.CANCELLED;
        this.cancelledReason = reason;
        this.cancelledAt = cancelledAt;
    }
}
