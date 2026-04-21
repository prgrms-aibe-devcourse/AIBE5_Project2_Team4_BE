package com.ieum.ansimdonghaeng.domain.recommendation.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.code.service.CodeValidationService;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.project.entity.Project;
import com.ieum.ansimdonghaeng.domain.project.repository.ProjectRepository;
import com.ieum.ansimdonghaeng.domain.recommendation.dto.request.FreelancerRecommendationRequest;
import com.ieum.ansimdonghaeng.domain.recommendation.dto.response.FreelancerRecommendationItemResponse;
import com.ieum.ansimdonghaeng.domain.recommendation.dto.response.FreelancerRecommendationResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private static final int DEFAULT_SIZE = 5;

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ProjectRepository projectRepository;
    private final CodeValidationService codeValidationService;
    private final AiRecommendationWeightService aiRecommendationWeightService;

    @Override
    public FreelancerRecommendationResponse recommendFreelancers(
            Long currentUserId,
            FreelancerRecommendationRequest request
    ) {
        RecommendationTarget target = resolveTarget(currentUserId, request);
        Optional<RecommendationWeights> aiWeights = aiRecommendationWeightService.requestWeights(target);
        RecommendationWeights weights = aiWeights.orElseGet(RecommendationWeights::defaults);
        boolean aiApplied = aiWeights.isPresent();
        List<ScoredFreelancer> scoredFreelancers = freelancerProfileRepository.findPublicRecommendationCandidates()
                .stream()
                .map(profile -> score(profile, target, weights))
                .sorted(Comparator
                        .comparingInt(ScoredFreelancer::matchScore).reversed()
                        .thenComparing(scored -> scored.profile().getAverageRating(), Comparator.reverseOrder())
                        .thenComparing(scored -> scored.profile().getActivityCount(), Comparator.reverseOrder())
                        .thenComparing(scored -> scored.profile().getId()))
                .toList();

        int limit = request.size() == null ? DEFAULT_SIZE : request.size();
        List<FreelancerRecommendationItemResponse> recommendations = new ArrayList<>();
        for (int index = 0; index < Math.min(limit, scoredFreelancers.size()); index++) {
            ScoredFreelancer scored = scoredFreelancers.get(index);
            recommendations.add(FreelancerRecommendationItemResponse.from(
                    index + 1,
                    scored.profile(),
                    scored.matchScore(),
                    scored.reHireRate(),
                    scored.matchReasons()
            ));
        }

        return new FreelancerRecommendationResponse(
                target.projectId(),
                target.projectTypeCode(),
                target.serviceRegionCode(),
                target.timeSlotCode(),
                target.requestedStartAt(),
                target.requestedEndAt(),
                aiApplied,
                aiApplied ? "AI_WEIGHTED" : "DEFAULT_WEIGHTED",
                weights.toResponse(),
                scoredFreelancers.size(),
                recommendations
        );
    }

    private RecommendationTarget resolveTarget(Long currentUserId, FreelancerRecommendationRequest request) {
        if (request.projectId() != null) {
            Project project = projectRepository.findById(request.projectId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
            if (!project.isOwnedBy(currentUserId)) {
                throw new CustomException(ErrorCode.PROJECT_ACCESS_DENIED);
            }
            return new RecommendationTarget(
                    project.getId(),
                    project.getProjectTypeCode(),
                    project.getServiceRegionCode(),
                    request.timeSlotCode(),
                    project.getRequestedStartAt(),
                    project.getRequestedEndAt()
            );
        }

        if (!StringUtils.hasText(request.projectTypeCode()) || !StringUtils.hasText(request.serviceRegionCode())) {
            throw new CustomException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "projectId or projectTypeCode/serviceRegionCode is required."
            );
        }

        codeValidationService.validateProjectTypeCode(request.projectTypeCode(), "projectTypeCode");
        codeValidationService.validateRegionCode(request.serviceRegionCode(), "serviceRegionCode");
        if (StringUtils.hasText(request.timeSlotCode())) {
            codeValidationService.validateAvailableTimeSlotCodes(Set.of(request.timeSlotCode()), "timeSlotCode");
        }

        return new RecommendationTarget(
                null,
                request.projectTypeCode(),
                request.serviceRegionCode(),
                request.timeSlotCode(),
                request.requestedStartAt(),
                request.requestedEndAt()
        );
    }

    private ScoredFreelancer score(FreelancerProfile profile, RecommendationTarget target, RecommendationWeights weights) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (profile.getProjectTypeCodes().contains(target.projectTypeCode())) {
            score += weights.projectTypeWeight();
            reasons.add("PROJECT_TYPE_MATCH");
        }

        if (profile.getActivityRegionCodes().contains(target.serviceRegionCode())) {
            score += weights.regionWeight();
            reasons.add("REGION_MATCH");
        }

        if (StringUtils.hasText(target.timeSlotCode())
                && profile.getAvailableTimeSlotCodes().contains(target.timeSlotCode())) {
            score += weights.timeSlotWeight();
            reasons.add("TIME_SLOT_MATCH");
        }

        if (Boolean.TRUE.equals(profile.getVerifiedYn())) {
            score += weights.verifiedWeight();
            reasons.add("VERIFIED_FREELANCER");
        }

        score += weightedRatingScore(profile.getAverageRating(), weights.ratingWeight());
        score += weightedActivityScore(profile.getActivityCount(), weights.activityWeight());

        if (profile.getAverageRating() != null && profile.getAverageRating().compareTo(new BigDecimal("4.50")) >= 0) {
            reasons.add("HIGH_RATING");
        }
        if (profile.getActivityCount() != null && profile.getActivityCount() >= 20) {
            reasons.add("EXPERIENCED");
        }

        return new ScoredFreelancer(profile, Math.min(score, 100), estimateReHireRate(profile), reasons);
    }

    private int ratingScore(BigDecimal averageRating) {
        if (averageRating == null) {
            return 0;
        }
        BigDecimal normalized = averageRating
                .max(BigDecimal.ZERO)
                .min(new BigDecimal("5.00"))
                .multiply(new BigDecimal("2"));
        return normalized.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private int weightedRatingScore(BigDecimal averageRating, int weight) {
        if (averageRating == null) {
            return 0;
        }
        BigDecimal normalized = averageRating
                .max(BigDecimal.ZERO)
                .min(new BigDecimal("5.00"))
                .divide(new BigDecimal("5.00"), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(weight));
        return normalized.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private int activityScore(Long activityCount) {
        long count = activityCount == null ? 0L : activityCount;
        if (count >= 30) {
            return 5;
        }
        if (count >= 10) {
            return 3;
        }
        return count > 0 ? 1 : 0;
    }

    private int weightedActivityScore(Long activityCount, int weight) {
        long count = activityCount == null ? 0L : activityCount;
        if (count <= 0) {
            return 0;
        }
        BigDecimal normalized = BigDecimal.valueOf(Math.min(count, 30L))
                .divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(weight));
        return normalized.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private int estimateReHireRate(FreelancerProfile profile) {
        int ratingPart = ratingScore(profile.getAverageRating()) * 4;
        int activityPart = Math.min((int) (profile.getActivityCount() == null ? 0 : profile.getActivityCount()), 40);
        return Math.min(95, 45 + ratingPart + activityPart / 2);
    }

    record RecommendationTarget(
            Long projectId,
            String projectTypeCode,
            String serviceRegionCode,
            String timeSlotCode,
            LocalDateTime requestedStartAt,
            LocalDateTime requestedEndAt
    ) {
    }

    private record ScoredFreelancer(
            FreelancerProfile profile,
            int matchScore,
            int reHireRate,
            List<String> matchReasons
    ) {
    }
}
