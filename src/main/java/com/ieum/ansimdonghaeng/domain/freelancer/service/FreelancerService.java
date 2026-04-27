package com.ieum.ansimdonghaeng.domain.freelancer.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.code.service.CodeValidationService;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.request.FreelancerProfileUpsertRequest;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.FreelancerDetailResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.FreelancerFileResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.FreelancerListResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.dto.response.PublicFreelancerDetailResponse;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerFile;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerFileRepository;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerProfileRepository;
import com.ieum.ansimdonghaeng.domain.user.entity.User;
import com.ieum.ansimdonghaeng.domain.user.entity.UserRole;
import com.ieum.ansimdonghaeng.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreelancerService {

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final FreelancerFileRepository freelancerFileRepository;
    private final UserRepository userRepository;
    private final FreelancerFileStorageService fileStorageService;
    private final CodeValidationService codeValidationService;

    @Transactional
    public FreelancerDetailResponse createMyProfile(Long currentUserId, FreelancerProfileUpsertRequest request) {
        if (freelancerProfileRepository.existsByUser_Id(currentUserId)) {
            throw new CustomException(ErrorCode.FREELANCER_PROFILE_DUPLICATE);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "User was not found."));

        if (Boolean.FALSE.equals(user.getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }

        validateProfileCodes(request);
        user.changeRole(UserRole.FREELANCER);
        FreelancerProfile profile = FreelancerProfile.create(
                user,
                request.careerDescription(),
                request.caregiverYn(),
                false,
                BigDecimal.ZERO,
                0L,
                request.publicYn(),
                request.activityRegionCodes(),
                request.availableTimeSlotCodes(),
                request.projectTypeCodes()
        );
        return FreelancerDetailResponse.from(freelancerProfileRepository.save(profile));
    }

    public FreelancerDetailResponse getMyProfile(Long currentUserId) {
        return FreelancerDetailResponse.from(getMyFreelancerProfile(currentUserId));
    }

    @Transactional
    public FreelancerDetailResponse updateMyProfile(Long currentUserId, FreelancerProfileUpsertRequest request) {
        FreelancerProfile profile = getMyFreelancerProfile(currentUserId);
        validateProfileCodes(request);
        profile.update(
                request.careerDescription(),
                request.caregiverYn(),
                request.publicYn(),
                request.activityRegionCodes(),
                request.availableTimeSlotCodes(),
                request.projectTypeCodes()
        );
        return FreelancerDetailResponse.from(profile);
    }

    // 공개 가능한 프리랜서만 페이지 단위로 조회한다.
    public FreelancerListResponse getFreelancers(String keyword, String projectType, String region, int page, int size) {
        String normalizedKeyword = normalize(keyword);
        String normalizedProjectType = normalize(projectType);
        String normalizedRegion = normalize(region);

        codeValidationService.validateProjectTypeCode(normalizedProjectType, "projectType");
        codeValidationService.validateRegionCode(normalizedRegion, "region");

        Page<FreelancerProfile> freelancerPage =
                freelancerProfileRepository.findPublicFreelancers(
                        normalizedKeyword,
                        normalizedProjectType,
                        normalizedRegion,
                        PageRequest.of(page, size)
                );
        return FreelancerListResponse.from(freelancerPage);
    }

    // 비공개 프로필이나 비활성 프리랜서는 상세 조회에서 숨긴다.
    public PublicFreelancerDetailResponse getFreelancer(Long freelancerProfileId) {
        return PublicFreelancerDetailResponse.from(getPublicFreelancerProfile(freelancerProfileId));
    }

    public FreelancerProfile getPublicFreelancerProfile(Long freelancerProfileId) {
        FreelancerProfile profile = freelancerProfileRepository.findDetailById(freelancerProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));
        validatePublicFreelancer(profile);
        return profile;
    }

    @Transactional
    public FreelancerFileResponse uploadMyFile(Long currentUserId, MultipartFile file) {
        FreelancerProfile profile = getMyFreelancerProfile(currentUserId);
        FreelancerFileStorageService.StoredFile storedFile = fileStorageService.storePortfolio(profile.getId(), file);
        int displayOrder = Math.toIntExact(freelancerFileRepository.countByFreelancerProfile_Id(profile.getId()) + 1);
        FreelancerFile freelancerFile = FreelancerFile.create(
                profile,
                storedFile.originalFilename(),
                storedFile.storedFilename(),
                storedFile.fileUrl(),
                file.getContentType(),
                file.getSize(),
                storedFile.fileData(),
                displayOrder
        );
        return FreelancerFileResponse.from(freelancerFileRepository.save(freelancerFile));
    }

    public List<FreelancerFileResponse> getMyFiles(Long currentUserId) {
        return freelancerFileRepository.findAllByUserId(currentUserId).stream()
                .map(FreelancerFileResponse::from)
                .toList();
    }

    @Transactional
    public void deleteMyFile(Long currentUserId, Long fileId) {
        FreelancerFile file = freelancerFileRepository.findDetailById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        freelancerFileRepository.delete(file);
        fileStorageService.delete(file.getFileUrl());
    }

    private void validatePublicFreelancer(FreelancerProfile profile) {
        if (!profile.isPublicProfile()
                || Boolean.FALSE.equals(profile.getUser().getActiveYn())
                || !UserRole.FREELANCER.getCode().equals(profile.getUser().getRoleCode())) {
            throw new CustomException(ErrorCode.FREELANCER_NOT_FOUND);
        }
    }

    public FreelancerProfile getMyFreelancerProfile(Long currentUserId) {
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.FREELANCER_NOT_FOUND));

        if (Boolean.FALSE.equals(profile.getUser().getActiveYn())) {
            throw new CustomException(ErrorCode.USER_INACTIVE);
        }

        return profile;
    }

    private void validateProfileCodes(FreelancerProfileUpsertRequest request) {
        codeValidationService.validateRegionCodes(request.activityRegionCodes(), "activityRegionCodes");
        codeValidationService.validateAvailableTimeSlotCodes(
                request.availableTimeSlotCodes(),
                "availableTimeSlotCodes"
        );
        codeValidationService.validateProjectTypeCodes(request.projectTypeCodes(), "projectTypeCodes");
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
