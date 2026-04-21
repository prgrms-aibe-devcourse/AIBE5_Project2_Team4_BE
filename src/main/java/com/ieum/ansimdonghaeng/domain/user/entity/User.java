package com.ieum.ansimdonghaeng.domain.user.entity;

import com.ieum.ansimdonghaeng.common.audit.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "APP_USER")
@SequenceGenerator(name = "app_user_seq_generator", sequenceName = "SEQ_APP_USER", allocationSize = 1)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq_generator")
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 510)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 510)
    private String passwordHash;

    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "PHONE", length = 40)
    private String phone;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "INTRO")
    private String intro;

    @Column(name = "ROLE_CODE", nullable = false, length = 50)
    private String roleCode;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private Boolean activeYn;

    @Transient
    @Builder.Default
    private String providerCode = AuthProvider.LOCAL.getCode();

    @Transient
    private String providerUserId;

    public void updateProfile(String name, String phone, String intro) {
        this.name = name;
        this.phone = phone;
        this.intro = intro;
    }

    public void changeRole(UserRole role) {
        this.roleCode = role.getCode();
    }

    public void deactivate() {
        this.activeYn = false;
    }

    public void updateActive(boolean active) {
        this.activeYn = active;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return UserRole.fromCode(roleCode);
    }
}
