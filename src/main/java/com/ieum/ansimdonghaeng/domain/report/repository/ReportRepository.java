package com.ieum.ansimdonghaeng.domain.report.repository;

import com.ieum.ansimdonghaeng.domain.report.entity.Report;
import com.ieum.ansimdonghaeng.domain.report.entity.ReportStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    long countByStatus(ReportStatus status);

    @Override
    Page<Report> findAll(Specification<Report> spec, Pageable pageable);

    boolean existsByReview_IdAndReporterUser_Id(Long reviewId, Long reporterUserId);

    @Query("""
            select distinct report.review.id
            from Report report
            where report.review.id in :reviewIds
            """)
    Set<Long> findReportedReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @EntityGraph(attributePaths = {"review", "review.project", "review.project.ownerUser", "reporterUser"})
    List<Report> findTop5ByStatusOrderByCreatedAtDescIdDesc(ReportStatus status);

    @Query("""
            select report
            from Report report
            join fetch report.review review
            join fetch review.project project
            join fetch report.reporterUser reporterUser
            left join fetch report.handledByUser handledByUser
            where report.id = :reportId
            """)
    Optional<Report> findDetailById(@Param("reportId") Long reportId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select report
            from Report report
            join fetch report.review review
            join fetch review.project project
            join fetch report.reporterUser reporterUser
            left join fetch report.handledByUser handledByUser
            where report.id = :reportId
            """)
    Optional<Report> findDetailByIdForUpdate(@Param("reportId") Long reportId);
}
