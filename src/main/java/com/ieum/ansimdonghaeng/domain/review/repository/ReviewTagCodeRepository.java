package com.ieum.ansimdonghaeng.domain.review.repository;

import com.ieum.ansimdonghaeng.domain.review.entity.ReviewTagCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewTagCodeRepository extends JpaRepository<ReviewTagCode, String> {

    boolean existsByCodeAndActiveYnTrue(String code);

    List<ReviewTagCode> findAllByCodeInAndActiveYnTrue(java.util.Collection<String> codes);

    List<ReviewTagCode> findAllByActiveYnTrueOrderBySortOrderAscCodeAsc();
}
