package com.ieum.ansimdonghaeng.domain.review.repository;

import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    long countByBlindedYn(String blindedYn);

    @Override
    Page<Review> findAll(Specification<Review> spec, Pageable pageable);

    boolean existsByProject_Id(Long projectId);

    Optional<Review> findByProject_Id(Long projectId);
}
