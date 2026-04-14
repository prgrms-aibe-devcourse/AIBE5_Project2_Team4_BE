package com.ieum.ansimdonghaeng.domain.user.entity;

public enum UserRole {
    USER,
    FREELANCER,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
