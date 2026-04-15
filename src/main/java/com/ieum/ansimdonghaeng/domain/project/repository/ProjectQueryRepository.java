package com.ieum.ansimdonghaeng.domain.project.repository;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectQueryRepository {

    // Oracle 구버전에서도 동작하도록 내 프로젝트 목록을 커스텀 페이지 쿼리로 조회한다.
    Page<Project> findMyProjects(Long ownerUserId, ProjectStatus status, Pageable pageable);
}
