package com.ieum.ansimdonghaeng.domain.project.repository;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectQueryRepository, JpaSpecificationExecutor<Project> {

    long countByStatus(ProjectStatus status);

    @Override
    Page<Project> findAll(Specification<Project> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser"})
    List<Project> findTop5ByOrderByCreatedAtDescIdDesc();

    // 단건 조회는 owner 조건을 함께 확인할 때도 사용한다.
    Optional<Project> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    // owner 기준으로 전체 프로젝트를 페이지 단위로 조회한다.
    Page<Project> findAllByOwnerUserId(Long ownerUserId, Pageable pageable);

    // owner와 status를 함께 적용한 프로젝트 목록을 조회한다.
    Page<Project> findAllByOwnerUserIdAndStatus(Long ownerUserId, ProjectStatus status, Pageable pageable);

    // 제안 수락처럼 경쟁 가능성이 있는 상태 변경에서는 프로젝트 행을 잠그고 조회한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select project from Project project where project.id = :projectId")
    Optional<Project> findByIdForUpdate(@Param("projectId") Long projectId);
}
