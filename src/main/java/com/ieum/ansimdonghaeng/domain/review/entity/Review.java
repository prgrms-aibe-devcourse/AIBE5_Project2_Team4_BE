package com.ieum.ansimdonghaeng.domain.review.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "REVIEWER_USER_ID", nullable = false)
    private Long reviewerUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEWER_USER_ID", insertable = false, updatable = false)
    private User reviewerUser;

    @Min(1)
    @Max(5)
    @Column(name = "RATING", nullable = false)
    private Integer rating;

    @Lob
    @Column(name = "CONTENT")
    private String content;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "BLINDED_YN", nullable = false, length = 1)
    private String blindedYn;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<ReviewTag> tags = new LinkedHashSet<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Review(Project project, Long reviewerUserId, Integer rating, String content, String blindedYn) {
        this.project = project;
        this.reviewerUserId = reviewerUserId;
        this.rating = rating;
        this.content = content;
        this.blindedYn = blindedYn;
    }

    public static Review create(Project project,
                                Long reviewerUserId,
                                Integer rating,
                                String content,
                                Collection<String> tagCodes) {
        Review review = Review.builder()
                .project(project)
                .reviewerUserId(reviewerUserId)
                .rating(rating)
                .content(content)
                .blindedYn("N")
                .build();
        review.replaceTags(tagCodes);
        return review;
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

    public boolean isWrittenBy(Long userId) {
        return Objects.equals(reviewerUserId, userId);
    }

    public void update(Integer rating, String content, Collection<String> tagCodes) {
        this.rating = rating;
        this.content = content;
        replaceTags(tagCodes);
    }

    private void replaceTags(Collection<String> tagCodes) {
        if (tagCodes == null) {
            tags.clear();
            return;
        }

        Set<String> requestedTagCodes = new LinkedHashSet<>();
        tagCodes.stream()
                .filter(Review::hasText)
                .map(String::trim)
                .forEach(requestedTagCodes::add);

        tags.removeIf(tag -> !requestedTagCodes.contains(tag.getTagCode()));

        Set<String> existingTagCodes = tags.stream()
                .map(ReviewTag::getTagCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        requestedTagCodes.stream()
                .filter(tagCode -> !existingTagCodes.contains(tagCode))
                .map(tagCode -> ReviewTag.create(this, tagCode))
                .forEach(tags::add);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
