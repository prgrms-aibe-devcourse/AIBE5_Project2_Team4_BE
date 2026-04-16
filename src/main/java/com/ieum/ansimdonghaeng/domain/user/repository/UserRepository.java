package com.ieum.ansimdonghaeng.domain.user.repository;

import com.ieum.ansimdonghaeng.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByActiveYnTrue();

    boolean existsByEmailIgnoreCase(String email);
}
