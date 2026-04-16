package com.ieum.ansimdonghaeng.domain.notice.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "NOTICE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "notice_seq_generator", sequenceName = "SEQ_NOTICE", allocationSize = 1)
public class Notice extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq_generator")
    @Column(name = "NOTICE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ADMIN_USER_ID", nullable = false)
    private User adminUser;

    @Column(name = "TITLE", nullable = false, length = 400)
    private String title;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "PUBLISHED_YN", nullable = false, length = 1)
    private Boolean publishedYn;

    @Column(name = "PUBLISHED_AT")
    private LocalDateTime publishedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Notice(User adminUser,
                   String title,
                   String content,
                   Boolean publishedYn,
                   LocalDateTime publishedAt) {
        this.adminUser = adminUser;
        this.title = title;
        this.content = content;
        this.publishedYn = publishedYn;
        this.publishedAt = publishedAt;
    }

    public static Notice create(User adminUser, String title, String content, boolean publishNow, LocalDateTime now) {
        return Notice.builder()
                .adminUser(adminUser)
                .title(title)
                .content(content)
                .publishedYn(publishNow)
                .publishedAt(publishNow ? now : null)
                .build();
    }

    public boolean isPublished() {
        return Boolean.TRUE.equals(publishedYn);
    }

    public void publish(LocalDateTime publishedAt) {
        this.publishedYn = true;
        this.publishedAt = publishedAt;
    }
}
