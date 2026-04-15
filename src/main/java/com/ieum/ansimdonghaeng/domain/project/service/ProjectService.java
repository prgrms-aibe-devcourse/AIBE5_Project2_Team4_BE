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

    // 현재 로그인한 사용자의 프로젝트를 생성한다.
    ProjectCreateResponse createProject(Long currentUserId, ProjectCreateRequest request);

    // 현재 로그인한 사용자의 프로젝트 목록을 상태 조건과 함께 조회한다.
    ProjectListResponse getMyProjects(Long currentUserId, ProjectStatus status, int page, int size);

    // 현재 로그인한 사용자가 소유한 프로젝트 상세를 조회한다.
    ProjectDetailResponse getProject(Long currentUserId, Long projectId);

    // 현재 로그인한 사용자가 소유한 REQUESTED 프로젝트를 부분 수정한다.
    ProjectDetailResponse updateProject(Long currentUserId, Long projectId, ProjectUpdateRequest request);

    // 현재 로그인한 사용자가 소유한 REQUESTED 프로젝트를 취소한다.
    ProjectCancelResponse cancelProject(Long currentUserId, Long projectId, ProjectCancelRequest request);
}
