package com.ieum.ansimdonghaeng.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record ReviewTagId(
        @Column(name = "REVIEW_ID")
        Long reviewId,

        @Column(name = "REVIEW_TAG_CODE", length = 60)
        String tagCode
) implements Serializable {
}
