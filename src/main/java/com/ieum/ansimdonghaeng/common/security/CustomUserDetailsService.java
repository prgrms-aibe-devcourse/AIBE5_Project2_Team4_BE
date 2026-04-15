package com.ieum.ansimdonghaeng.common.security;

import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!StringUtils.hasText(username)) {
            throw new UsernameNotFoundException("Username must not be blank.");
        }

        return CustomUserDetails.builder()
                // TODO: 공통 user 도메인이 준비되면 실제 사용자 PK 조회로 교체한다.
                .userId(resolveBootstrapUserId(username))
                .username(username)
                .password("{noop}bootstrap-password")
                .authorities(List.of(new SimpleGrantedAuthority(UserRole.USER.asAuthority())))
                .enabled(true)
                .build();
    }

    // auth 스켈레톤 단계에서는 username 기준의 안정적인 임시 userId를 사용한다.
    private Long resolveBootstrapUserId(String username) {
        if (username.chars().allMatch(Character::isDigit)) {
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException ignored) {
                // 숫자 파싱이 불가능하면 아래 해시 기반 fallback을 사용한다.
            }
        }
        return Integer.toUnsignedLong(username.hashCode());
    }
}
