package com.ieum.ansimdonghaeng.domain.notice.repository;

import com.ieum.ansimdonghaeng.domain.notice.entity.Notice;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    long countByPublishedYnTrue();

    Page<Notice> findAllByPublishedYnTrue(Pageable pageable);

    @Query("""
            select notice
            from Notice notice
            join fetch notice.adminUser adminUser
            where notice.id = :noticeId
            """)
    Optional<Notice> findDetailById(@Param("noticeId") Long noticeId);
}
