package com.ieum.ansimdonghaeng.domain.review.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import jakarta.persistence.Column;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "REVIEW")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "review_seq_generator",
    sequenceName = "SEQ_REVIEW",
    allocationSize = 1
)
public class Review extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_seq_generator")
    @Column(name = "REVIEW_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false, unique = true)
    private Project project;

    @Min(1)
    @Max(5)
    @Column(name = "RATING", nullable = false)
    private Integer rating;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Pattern(regexp = "Y|N")
    @Column(name = "BLINDED_YN", nullable = false, length = 1)
    private String blindedYn;

    @Builder
    private Review(Project project, Integer rating, String content, String blindedYn) {
        this.project = project;
        this.rating = rating;
        this.content = content;
        this.blindedYn = blindedYn;
    }

    public static Review create(Project project, Integer rating, String content) {
        return Review.builder()
            .project(project)
            .rating(rating)
            .content(content)
            .blindedYn("N")
            .build();
    }

    public void blind() {
        this.blindedYn = "Y";
    }

    public void unblind() {
        this.blindedYn = "N";
    }

    public boolean isBlinded() {
        return "Y".equalsIgnoreCase(blindedYn);
    }

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
