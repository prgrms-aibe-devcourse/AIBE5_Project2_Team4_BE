package com.ieum.ansimdonghaeng.domain.auth.repository;

import com.ieum.ansimdonghaeng.domain.auth.entity.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    List<RefreshToken> findAllByUser_IdAndActiveYnTrue(Long userId);
}
