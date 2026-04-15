package com.ieum.ansimdonghaeng.domain.project.repository;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectQueryRepositoryImpl implements ProjectQueryRepository {

    private static final String PROJECT_COLUMNS = """
            p.PROJECT_ID,
            p.OWNER_USER_ID,
            p.PROJECT_TYPE_CODE,
            p.SERVICE_REGION_CODE,
            p.TITLE,
            p.REQUESTED_START_AT,
            p.REQUESTED_END_AT,
            p.SERVICE_ADDRESS,
            p.SERVICE_DETAIL_ADDRESS,
            p.REQUEST_DETAIL,
            p.STATUS_CODE,
            p.ACCEPTED_AT,
            p.STARTED_AT,
            p.COMPLETED_AT,
            p.CANCELLED_AT,
            p.CANCELLED_REASON,
            p.CREATED_AT,
            p.UPDATED_AT
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Project> findMyProjects(Long ownerUserId, ProjectStatus status, Pageable pageable) {
        String whereClause = buildWhereClause(status);
        String contentQuery = """
                SELECT
                    PROJECT_ID,
                    OWNER_USER_ID,
                    PROJECT_TYPE_CODE,
                    SERVICE_REGION_CODE,
                    TITLE,
                    REQUESTED_START_AT,
                    REQUESTED_END_AT,
                    SERVICE_ADDRESS,
                    SERVICE_DETAIL_ADDRESS,
                    REQUEST_DETAIL,
                    STATUS_CODE,
                    ACCEPTED_AT,
                    STARTED_AT,
                    COMPLETED_AT,
                    CANCELLED_AT,
                    CANCELLED_REASON,
                    CREATED_AT,
                    UPDATED_AT
                FROM (
                    SELECT
                """ + PROJECT_COLUMNS + """
                        ,
                        ROW_NUMBER() OVER (ORDER BY p.CREATED_AT DESC) AS rn
                    FROM PROJECT p
                """ + whereClause + """
                )
                WHERE rn > :offsetRow
                  AND rn <= :endRow
                ORDER BY rn
                """;

        Query query = entityManager.createNativeQuery(contentQuery, Project.class);
        bindParameters(query, ownerUserId, status);
        query.setParameter("offsetRow", pageable.getOffset());
        query.setParameter("endRow", pageable.getOffset() + pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Project> content = query.getResultList();

        String countQuery = "SELECT COUNT(*) FROM PROJECT p " + whereClause;
        Query totalQuery = entityManager.createNativeQuery(countQuery);
        bindParameters(totalQuery, ownerUserId, status);
        long total = ((Number) totalQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildWhereClause(ProjectStatus status) {
        if (status == null) {
            return " WHERE p.OWNER_USER_ID = :ownerUserId ";
        }
        return " WHERE p.OWNER_USER_ID = :ownerUserId AND p.STATUS_CODE = :status ";
    }

    private void bindParameters(Query query, Long ownerUserId, ProjectStatus status) {
        query.setParameter("ownerUserId", ownerUserId);
        if (status != null) {
            query.setParameter("status", status.name());
        }
    }
}
