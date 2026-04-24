package com.ieum.ansimdonghaeng.domain.project.service;

import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCancelRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectCreateRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.request.ProjectUpdateRequest;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectCancelResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectCreateResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectDetailResponse;
import com.ieum.ansimdonghaeng.domain.project.dto.response.ProjectListResponse;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;

public interface ProjectService {

    ProjectCreateResponse createProject(Long currentUserId, ProjectCreateRequest request);

    ProjectListResponse getMyProjects(Long currentUserId, ProjectStatus status, int page, int size);

    ProjectListResponse getRecruitingProjects(int page, int size);

    ProjectDetailResponse getProject(Long currentUserId, Long projectId);

    ProjectDetailResponse updateProject(Long currentUserId, Long projectId, ProjectUpdateRequest request);

    ProjectCancelResponse cancelProject(Long currentUserId, Long projectId, ProjectCancelRequest request);

    ProjectDetailResponse startProject(Long currentUserId, boolean adminOverride, Long projectId);

    ProjectDetailResponse completeProject(Long currentUserId, boolean adminOverride, Long projectId);
}
