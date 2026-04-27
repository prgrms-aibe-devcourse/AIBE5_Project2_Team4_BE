package com.ieum.ansimdonghaeng.domain.verification.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerProfile;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerFileStorageService;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerService;
import com.ieum.ansimdonghaeng.domain.verification.dto.request.VerificationCreateRequest;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationFileResponse;
import com.ieum.ansimdonghaeng.domain.verification.dto.response.VerificationResponse;
import com.ieum.ansimdonghaeng.domain.verification.entity.Verification;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationStatus;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final VerificationFileRepository verificationFileRepository;
    private final FreelancerService freelancerService;
    private final FreelancerFileStorageService fileStorageService;

    @Transactional
    public VerificationResponse createMyVerification(Long currentUserId, VerificationCreateRequest request) {
        FreelancerProfile profile = freelancerService.getMyFreelancerProfile(currentUserId);
        if (verificationRepository.existsByFreelancerProfile_IdAndVerificationTypeAndStatus(
                profile.getId(),
                request.type(),
                VerificationStatus.PENDING
        )) {
            throw new CustomException(ErrorCode.VERIFICATION_DUPLICATE);
        }

        Verification verification = Verification.create(
                profile,
                request.type(),
                request.requestMessage(),
                java.time.LocalDateTime.now()
        );
        return VerificationResponse.from(verificationRepository.save(verification));
    }

    public List<VerificationResponse> getMyVerifications(Long currentUserId) {
        return verificationRepository.findAllByUserId(currentUserId).stream()
                .map(VerificationResponse::from)
                .toList();
    }

    public VerificationResponse getMyVerification(Long currentUserId, Long verificationId) {
        Verification verification = verificationRepository.findDetailById(verificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        return VerificationResponse.from(verification);
    }

    @Transactional
    public VerificationFileResponse uploadMyVerificationFile(Long currentUserId,
                                                             Long verificationId,
                                                             MultipartFile file) {
        Verification verification = getMutableOwnedVerification(currentUserId, verificationId);
        FreelancerProfile profile = verification.getFreelancerProfile();
        FreelancerFileStorageService.StoredFile storedFile = fileStorageService.storeVerification(
                profile.getId(),
                verification.getId(),
                file
        );
        VerificationFile verificationFile = VerificationFile.create(
                verification,
                storedFile.originalFilename(),
                storedFile.storedFilename(),
                storedFile.fileUrl(),
                file.getContentType(),
                file.getSize(),
                storedFile.fileData(),
                java.time.LocalDateTime.now()
        );
        return VerificationFileResponse.from(verificationFileRepository.save(verificationFile));
    }

    public List<VerificationFileResponse> getMyVerificationFiles(Long currentUserId, Long verificationId) {
        getOwnedVerification(currentUserId, verificationId);
        return verificationFileRepository.findAllByVerificationIdAndUserId(verificationId, currentUserId).stream()
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
        validateFileMutableVerification(file.getVerification());

        verificationFileRepository.delete(file);
        fileStorageService.delete(file.getFileUrl());
    }

    private Verification getOwnedVerification(Long currentUserId, Long verificationId) {
        Verification verification = verificationRepository.findDetailById(verificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (!verification.isOwnedBy(currentUserId)) {
            throw new CustomException(ErrorCode.VERIFICATION_NOT_FOUND);
        }

        return verification;
    }

    private Verification getMutableOwnedVerification(Long currentUserId, Long verificationId) {
        Verification verification = getOwnedVerification(currentUserId, verificationId);
        validateFileMutableVerification(verification);
        return verification;
    }

    private void validateFileMutableVerification(Verification verification) {
        if (verification.getStatus() != VerificationStatus.PENDING
                && verification.getStatus() != VerificationStatus.APPROVED) {
            throw new CustomException(ErrorCode.VERIFICATION_INVALID_STATUS);
        }
    }
}
