package com.ieum.ansimdonghaeng.domain.project.repository;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectQueryRepository {

    // 단건 조회와 owner 조건을 함께 사용하고 싶을 때 활용한다.
    Optional<Project> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    // owner 기준으로 전체 프로젝트를 페이지 단위 조회한다.
    Page<Project> findAllByOwnerUserId(Long ownerUserId, Pageable pageable);

    // owner와 status를 함께 적용해 프로젝트를 페이지 단위 조회한다.
    Page<Project> findAllByOwnerUserIdAndStatus(Long ownerUserId, ProjectStatus status, Pageable pageable);
}
