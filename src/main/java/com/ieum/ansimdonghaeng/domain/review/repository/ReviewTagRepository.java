package com.ieum.ansimdonghaeng.domain.review.repository;

import com.ieum.ansimdonghaeng.domain.review.entity.ReviewTag;
import com.ieum.ansimdonghaeng.domain.review.entity.ReviewTagId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, ReviewTagId> {

    List<ReviewTag> findAllByReview_IdIn(Collection<Long> reviewIds);
}
