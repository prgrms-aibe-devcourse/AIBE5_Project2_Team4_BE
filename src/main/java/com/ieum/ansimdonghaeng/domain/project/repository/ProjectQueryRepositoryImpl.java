package com.ieum.ansimdonghaeng.domain.project.repository;

import static com.ieum.ansimdonghaeng.domain.project.entity.QProject.project;

import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepositoryImpl implements ProjectQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProjectSummaryView> findMyProjects(Long ownerUserId, ProjectStatus status, Pageable pageable) {
        BooleanExpression statusCondition = status == null ? null : project.status.eq(status);

        List<ProjectSummaryView> content = queryFactory
                .select(Projections.constructor(
                        ProjectSummaryView.class,
                        project.id,
                        project.title,
                        project.projectTypeCode,
                        project.serviceRegionCode,
                        project.requestedStartAt,
                        project.requestedEndAt,
                        project.status,
                        project.createdAt,
                        project.updatedAt
                ))
                .from(project)
                .where(project.ownerUserId.eq(ownerUserId), statusCondition)
                .orderBy(project.createdAt.desc(), project.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.ownerUserId.eq(ownerUserId), statusCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<ProjectSummaryView> findRecruitingProjects(Pageable pageable) {
        List<ProjectSummaryView> content = queryFactory
                .select(Projections.constructor(
                        ProjectSummaryView.class,
                        project.id,
                        project.title,
                        project.projectTypeCode,
                        project.serviceRegionCode,
                        project.requestedStartAt,
                        project.requestedEndAt,
                        project.status,
                        project.createdAt,
                        project.updatedAt
                ))
                .from(project)
                .where(project.status.eq(ProjectStatus.REQUESTED))
                .orderBy(project.createdAt.desc(), project.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.status.eq(ProjectStatus.REQUESTED))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
