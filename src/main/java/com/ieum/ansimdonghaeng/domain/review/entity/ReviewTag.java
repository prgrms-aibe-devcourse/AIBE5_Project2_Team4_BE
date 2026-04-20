package com.ieum.ansimdonghaeng.domain.review.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REVIEW_TAG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTag {

    @EmbeddedId
    private ReviewTagId id;

    @MapsId("reviewId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    private ReviewTag(Review review, String tagCode) {
        this.review = review;
        this.id = new ReviewTagId(null, tagCode);
    }

    public static ReviewTag create(Review review, String tagCode) {
        return new ReviewTag(review, tagCode);
    }

    public String getTagCode() {
        return id != null ? id.tagCode() : null;
    }
}
