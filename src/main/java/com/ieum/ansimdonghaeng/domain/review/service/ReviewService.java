package com.ieum.ansimdonghaeng.domain.review.service;

import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.entity.ProjectStatus;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.review.dto.request.ReviewCreateRequest;
import com.ieum.ansimdonghaeng.domain.review.entity.Review;
import com.ieum.ansimdonghaeng.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;

    public Review createReview(Long projectId, ReviewCreateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new IllegalStateException("Review can only be created for completed projects.");
        }

        if (reviewRepository.existsByProject_Id(projectId)) {
            throw new IllegalStateException("Review already exists for project: " + projectId);
        }

        // tag is intentionally ignored for now.
        Review review = Review.create(project, request.rating(), request.content());
        return reviewRepository.save(review);
    }
}
