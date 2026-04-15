package com.ieum.ansimdonghaeng.domain.project.entity;

public enum ProjectStatus {
    // 사용자가 프로젝트를 생성하고 아직 매칭 전인 상태다.
    REQUESTED,
    // 프리랜서가 수락한 이후 상태다.
    ACCEPTED,
    // 서비스 진행이 시작된 상태다.
    IN_PROGRESS,
    // 서비스가 정상 완료된 상태다.
    COMPLETED,
    // 요청자가 취소한 상태다.
    CANCELLED
}
