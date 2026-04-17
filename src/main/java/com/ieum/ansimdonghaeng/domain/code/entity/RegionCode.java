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
@Table(name = "REGION_CODE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCode {

    @Id
    @Column(name = "REGION_CODE", length = 40)
    private String code;

    @Column(name = "PARENT_REGION_CODE", length = 40)
    private String parentRegionCode;

    @Column(name = "REGION_NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "REGION_LEVEL", nullable = false)
    private Integer regionLevel;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private Boolean activeYn;
}
