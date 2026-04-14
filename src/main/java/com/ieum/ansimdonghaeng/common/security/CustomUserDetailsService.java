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
                .username(username)
                .password("{noop}bootstrap-password")
                .authorities(List.of(new SimpleGrantedAuthority(UserRole.USER.asAuthority())))
                .enabled(true)
                .build();
    }
}
