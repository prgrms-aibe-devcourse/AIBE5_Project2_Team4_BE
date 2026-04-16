package com.ieum.ansimdonghaeng.domain.verification.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerFileStorageService;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationCreateRequest;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationReviewRequest;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationFileResponse;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationListResponse;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationResponse;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationRequest;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final VerificationFileRepository verificationFileRepository;
    private final FreelancerService freelancerService;
    private final FreelancerFileStorageService fileStorageService;

    @Transactional
    public VerificationResponse createMyVerification(Long currentUserId, VerificationCreateRequest request) {
        FreelancerProfile profile = freelancerService.getMyFreelancerProfile(currentUserId);
        if (verificationRequestRepository.existsByFreelancerProfile_IdAndTypeAndStatus(
                profile.getId(),
                request.type(),
                VerificationStatus.PENDING
        )) {
            throw new CustomException(ErrorCode.VERIFICATION_DUPLICATE);
        }

        VerificationRequest verificationRequest = VerificationRequest.create(
                profile,
                request.type(),
                request.requestMessage()
        );
        return VerificationResponse.from(verificationRequestRepository.save(verificationRequest));
    }

    public List<VerificationResponse> getMyVerifications(Long currentUserId) {
        return verificationRequestRepository.findAllByUserId(currentUserId).stream()
                .map(VerificationResponse::from)
                .toList();
    }

    public VerificationResponse getMyVerification(Long currentUserId, Long verificationRequestId) {
        VerificationRequest verificationRequest = verificationRequestRepository.findDetailById(verificationRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verificationRequest.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        return VerificationResponse.from(verificationRequest);
    }

    @Transactional
    public VerificationFileResponse uploadMyVerificationFile(Long currentUserId,
                                                             Long verificationRequestId,
                                                             MultipartFile file) {
        VerificationRequest verificationRequest = getOwnedVerification(currentUserId, verificationRequestId);
        FreelancerProfile profile = verificationRequest.getFreelancerProfile();
        FreelancerFileStorageService.StoredFile storedFile = fileStorageService.storeVerification(
                profile.getId(),
                verificationRequest.getId(),
                file
        );
        VerificationFile verificationFile = VerificationFile.create(
                verificationRequest,
                storedFile.originalFilename(),
                storedFile.storedFilename(),
                storedFile.fileUrl(),
                file.getContentType(),
                file.getSize()
        );
        return VerificationFileResponse.from(verificationFileRepository.save(verificationFile));
    }

    public List<VerificationFileResponse> getMyVerificationFiles(Long currentUserId, Long verificationRequestId) {
        getOwnedVerification(currentUserId, verificationRequestId);
        return verificationFileRepository.findAllByVerificationIdAndUserId(verificationRequestId, currentUserId).stream()
                .map(VerificationFileResponse::from)
                .toList();
    }

    @Transactional
    public void deleteMyVerificationFile(Long currentUserId, Long fileId) {
        VerificationFile file = verificationFileRepository.findDetailById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        verificationFileRepository.delete(file);
        fileStorageService.delete(file.getFileUrl());
    }

    public VerificationListResponse getVerificationsForAdmin(VerificationStatus status, int page, int size) {
        return VerificationListResponse.from(verificationRequestRepository.findAllWithProfile(
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @Transactional
    public VerificationResponse approveForAdmin(Long adminUserId,
                                                Long verificationRequestId,
                                                VerificationReviewRequest request) {
        VerificationRequest verificationRequest = getPendingVerification(verificationRequestId);
        verificationRequest.approve(adminUserId, LocalDateTime.now());
        return VerificationResponse.from(verificationRequest);
    }

    @Transactional
    public VerificationResponse rejectForAdmin(Long adminUserId,
                                               Long verificationRequestId,
                                               VerificationReviewRequest request) {
        VerificationRequest verificationRequest = getPendingVerification(verificationRequestId);
        verificationRequest.reject(adminUserId, request.reviewComment(), LocalDateTime.now());
        return VerificationResponse.from(verificationRequest);
    }

    private VerificationRequest getPendingVerification(Long verificationRequestId) {
        VerificationRequest verificationRequest = verificationRequestRepository.findDetailById(verificationRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verificationRequest.getStatus() != VerificationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "Only pending verification requests can be reviewed.");
        }

        return verificationRequest;
    }

    private VerificationRequest getOwnedVerification(Long currentUserId, Long verificationRequestId) {
        VerificationRequest verificationRequest = verificationRequestRepository.findDetailById(verificationRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verificationRequest.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        return verificationRequest;
    }
}
