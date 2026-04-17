package com.ieum.ansimdonghaeng.domain.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "AVAILABLE_TIME_SLOT_CODE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailableTimeSlotCode {

    @Id
    @Column(name = "TIME_SLOT_CODE", length = 60)
    private String code;

    @Column(name = "TIME_SLOT_NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "START_MINUTE")
    private Integer startMinute;

    @Column(name = "END_MINUTE")
    private Integer endMinute;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private Boolean activeYn;
}
