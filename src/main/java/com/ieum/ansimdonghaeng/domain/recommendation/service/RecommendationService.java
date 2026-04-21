package com.ieum.ansimdonghaeng.domain.recommendation.service;

import com.ieum.ansimdonghaeng.domain.recommendation.dto.request.FreelancerRecommendationRequest;
import com.ieum.ansimdonghaeng.domain.recommendation.dto.response.FreelancerRecommendationResponse;

public interface RecommendationService {

    FreelancerRecommendationResponse recommendFreelancers(Long currentUserId, FreelancerRecommendationRequest request);
}
