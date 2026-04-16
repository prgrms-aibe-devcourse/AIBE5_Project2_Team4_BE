package com.ieum.ansimdonghaeng.domain.freelancer.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Basic;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "FREELANCER_PROFILE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "freelancer_profile_seq_generator", sequenceName = "SEQ_FL_PROFILE", allocationSize = 1)
public class FreelancerProfile extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "freelancer_profile_seq_generator")
    @Column(name = "FREELANCER_PROFILE_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    // 프리랜서 자기소개/경력 설명은 Oracle 스키마 기준 CLOB으로 관리한다.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "CAREER_DESCRIPTION")
    private String careerDescription;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "CAREGIVER_YN", nullable = false, length = 1)
    private Boolean caregiverYn;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "VERIFIED_YN", nullable = false, length = 1)
    private Boolean verifiedYn;

    @Column(name = "AVERAGE_RATING", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "ACTIVITY_COUNT", nullable = false)
    private Long activityCount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "PUBLIC_YN", nullable = false, length = 1)
    private Boolean publicYn;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "FREELANCER_ACTIVITY_REGION", joinColumns = @JoinColumn(name = "FREELANCER_PROFILE_ID"))
    @Column(name = "REGION_CODE", nullable = false, length = 20)
    private Set<String> activityRegionCodes = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "FREELANCER_AVAILABLE_SLOT", joinColumns = @JoinColumn(name = "FREELANCER_PROFILE_ID"))
    @Column(name = "TIME_SLOT_CODE", nullable = false, length = 30)
    private Set<String> availableTimeSlotCodes = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "FREELANCER_PROJECT_TYPE", joinColumns = @JoinColumn(name = "FREELANCER_PROFILE_ID"))
    @Column(name = "PROJECT_TYPE_CODE", nullable = false, length = 30)
    private Set<String> projectTypeCodes = new LinkedHashSet<>();

    @Builder(access = AccessLevel.PRIVATE)
    private FreelancerProfile(User user,
                              String careerDescription,
                              Boolean caregiverYn,
                              Boolean verifiedYn,
                              BigDecimal averageRating,
                              Long activityCount,
                              Boolean publicYn,
                              Set<String> activityRegionCodes,
                              Set<String> availableTimeSlotCodes,
                              Set<String> projectTypeCodes) {
        this.user = user;
        this.careerDescription = careerDescription;
        this.caregiverYn = caregiverYn;
        this.verifiedYn = verifiedYn;
        this.averageRating = averageRating;
        this.activityCount = activityCount;
        this.publicYn = publicYn;
        this.activityRegionCodes = new LinkedHashSet<>(activityRegionCodes);
        this.availableTimeSlotCodes = new LinkedHashSet<>(availableTimeSlotCodes);
        this.projectTypeCodes = new LinkedHashSet<>(projectTypeCodes);
    }

    // 조회/제안 테스트용 기본 프로필을 간단히 만들 수 있도록 생성 메서드를 둔다.
    public static FreelancerProfile create(User user,
                                           String careerDescription,
                                           Boolean caregiverYn,
                                           Boolean verifiedYn,
                                           BigDecimal averageRating,
                                           Long activityCount,
                                           Boolean publicYn,
                                           Set<String> activityRegionCodes,
                                           Set<String> availableTimeSlotCodes,
                                           Set<String> projectTypeCodes) {
        return FreelancerProfile.builder()
                .user(user)
                .careerDescription(careerDescription)
                .caregiverYn(Boolean.TRUE.equals(caregiverYn))
                .verifiedYn(Boolean.TRUE.equals(verifiedYn))
                .averageRating(averageRating)
                .activityCount(activityCount)
                .publicYn(Boolean.TRUE.equals(publicYn))
                .activityRegionCodes(activityRegionCodes == null ? Set.of() : activityRegionCodes)
                .availableTimeSlotCodes(availableTimeSlotCodes == null ? Set.of() : availableTimeSlotCodes)
                .projectTypeCodes(projectTypeCodes == null ? Set.of() : projectTypeCodes)
                .build();
    }

    public boolean isOwnedBy(Long userId) {
        return Objects.equals(user.getId(), userId);
    }

    public boolean isPublicProfile() {
        return Boolean.TRUE.equals(publicYn);
    }

    public void update(String careerDescription,
                       Boolean caregiverYn,
                       Boolean publicYn,
                       Set<String> activityRegionCodes,
                       Set<String> availableTimeSlotCodes,
                       Set<String> projectTypeCodes) {
        this.careerDescription = careerDescription;
        this.caregiverYn = Boolean.TRUE.equals(caregiverYn);
        this.publicYn = Boolean.TRUE.equals(publicYn);
        this.activityRegionCodes.clear();
        this.activityRegionCodes.addAll(activityRegionCodes == null ? Set.of() : activityRegionCodes);
        this.availableTimeSlotCodes.clear();
        this.availableTimeSlotCodes.addAll(availableTimeSlotCodes == null ? Set.of() : availableTimeSlotCodes);
        this.projectTypeCodes.clear();
        this.projectTypeCodes.addAll(projectTypeCodes == null ? Set.of() : projectTypeCodes);
    }

    public void markVerified() {
        this.verifiedYn = true;
    }
}
