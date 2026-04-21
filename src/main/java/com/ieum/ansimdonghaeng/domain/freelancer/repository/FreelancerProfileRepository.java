package com.ieum.ansimdonghaeng.domain.freelancer.repository;

import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long>, FreelancerQueryRepository,
        JpaSpecificationExecutor<FreelancerProfile> {

    long countByVerifiedYnTrue();

    @Override
    Page<FreelancerProfile> findAll(Specification<FreelancerProfile> spec, Pageable pageable);

    // 현재 로그인한 프리랜서 사용자의 프로필을 조회할 때 사용한다.
    @Query("select profile from FreelancerProfile profile join fetch profile.user where profile.user.id = :userId")
    Optional<FreelancerProfile> findByUserId(@Param("userId") Long userId);

    boolean existsByUser_Id(Long userId);

    // 상세 응답에서 사용자 정보까지 함께 쓰기 위해 프로필과 사용자 정보를 같이 조회한다.
    @Query("select profile from FreelancerProfile profile join fetch profile.user where profile.id = :freelancerProfileId")
    Optional<FreelancerProfile> findDetailById(@Param("freelancerProfileId") Long freelancerProfileId);
}
