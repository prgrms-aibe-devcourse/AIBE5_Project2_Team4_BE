package com.ieum.ansimdonghaeng.domain.proposal.repository;

import static com.ieum.ansimdonghaeng.domain.freelancer.entity.QFreelancerProfile.freelancerProfile;
import static com.ieum.ansimdonghaeng.domain.project.entity.QProject.project;
import static com.ieum.ansimdonghaeng.domain.proposal.entity.QProposal.proposal;
import static com.ieum.ansimdonghaeng.domain.user.entity.QUser.user;

import com.ieum.ansimdonghaeng.domain.proposal.entity.ProposalStatus;
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
public class ProposalQueryRepositoryImpl implements ProposalQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProposalSummaryView> findFreelancerProposals(Long freelancerProfileId, ProposalStatus status, Pageable pageable) {
        BooleanExpression statusCondition = status == null ? null : proposal.status.eq(status);

        List<ProposalSummaryView> content = queryFactory
                .select(Projections.constructor(
                        ProposalSummaryView.class,
                        proposal.id,
                        project.id,
                        project.title,
                        project.ownerUserId,
                        proposal.status,
                        project.status,
                        proposal.message,
                        proposal.respondedAt,
                        proposal.createdAt,
                        proposal.updatedAt
                ))
                .from(proposal)
                .join(proposal.project, project)
                .where(proposal.freelancerProfile.id.eq(freelancerProfileId), statusCondition)
                .orderBy(proposal.createdAt.desc(), proposal.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(proposal.count())
                .from(proposal)
                .where(proposal.freelancerProfile.id.eq(freelancerProfileId), statusCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<ProjectProposalSummaryView> findProjectOwnerProposals(Long projectId, ProposalStatus status, Pageable pageable) {
        BooleanExpression statusCondition = status == null ? null : proposal.status.eq(status);

        List<ProjectProposalSummaryView> content = queryFactory
                .select(Projections.constructor(
                        ProjectProposalSummaryView.class,
                        proposal.id,
                        project.id,
                        freelancerProfile.id,
                        user.id,
                        user.name,
                        freelancerProfile.verifiedYn,
                        freelancerProfile.averageRating,
                        proposal.status,
                        proposal.message,
                        proposal.createdAt,
                        proposal.respondedAt
                ))
                .from(proposal)
                .join(proposal.project, project)
                .join(proposal.freelancerProfile, freelancerProfile)
                .join(freelancerProfile.user, user)
                .where(project.id.eq(projectId), statusCondition)
                .orderBy(proposal.createdAt.desc(), proposal.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(proposal.count())
                .from(proposal)
                .where(proposal.project.id.eq(projectId), statusCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
