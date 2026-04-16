package com.ieum.ansimdonghaeng.domain.auth.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "refresh_token_seq_generator", sequenceName = "SEQ_REFRESH_TOKEN", allocationSize = 1)
public class RefreshToken extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq_generator")
    @Column(name = "REFRESH_TOKEN_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "TOKEN_VALUE", nullable = false, length = 1000)
    private String tokenValue;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "REVOKED_AT")
    private LocalDateTime revokedAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private Boolean activeYn;

    @Builder(access = AccessLevel.PRIVATE)
    private RefreshToken(User user,
                         String tokenValue,
                         LocalDateTime expiresAt,
                         LocalDateTime revokedAt,
                         Boolean activeYn) {
        this.user = user;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.activeYn = activeYn;
    }

    public static RefreshToken issue(User user, String tokenValue, LocalDateTime expiresAt) {
        return RefreshToken.builder()
                .user(user)
                .tokenValue(tokenValue)
                .expiresAt(expiresAt)
                .activeYn(true)
                .build();
    }

    public boolean isUsable(LocalDateTime now) {
        return Boolean.TRUE.equals(activeYn)
                && revokedAt == null
                && expiresAt.isAfter(now);
    }

    public void revoke(LocalDateTime revokedAt) {
        this.activeYn = false;
        this.revokedAt = revokedAt;
    }
}
