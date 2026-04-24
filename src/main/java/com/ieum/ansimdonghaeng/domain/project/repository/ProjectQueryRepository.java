package com.ieum.ansimdonghaeng.domain.project.repository;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectQueryRepository {

    Page<ProjectSummaryView> findMyProjects(Long ownerUserId, ProjectStatus status, Pageable pageable);

    Page<ProjectSummaryView> findRecruitingProjects(Pageable pageable);
}
