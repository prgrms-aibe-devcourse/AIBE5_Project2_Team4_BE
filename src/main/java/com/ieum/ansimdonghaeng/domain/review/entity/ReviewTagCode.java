package com.ieum.ansimdonghaeng.domain.review.entity;

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
@Table(name = "REVIEW_TAG_CODE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTagCode {

    @Id
    @Column(name = "REVIEW_TAG_CODE", length = 60)
    private String code;

    @Column(name = "REVIEW_TAG_NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private Boolean activeYn;

    ReviewTagCode(String code) {
        this.code = code;
    }
}
