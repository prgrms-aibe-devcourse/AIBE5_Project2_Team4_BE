package com.ieum.ansimdonghaeng.domain.file.service;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import com.ieum.ansimdonghaeng.common.security.CustomUserDetails;
import com.ieum.ansimdonghaeng.domain.file.support.FileKeySupport;
import com.ieum.ansimdonghaeng.domain.freelancer.entity.FreelancerFile;
import com.ieum.ansimdonghaeng.domain.freelancer.service.FreelancerFileStorageService;
import com.ieum.ansimdonghaeng.domain.freelancer.repository.FreelancerFileRepository;
import com.ieum.ansimdonghaeng.domain.verification.entity.VerificationFile;
import com.ieum.ansimdonghaeng.domain.verification.repository.VerificationFileRepository;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FreelancerFileRepository freelancerFileRepository;
    private final VerificationFileRepository verificationFileRepository;
    private final FreelancerFileStorageService fileStorageService;

    public DownloadableFile loadInline(CustomUserDetails userDetails, String fileKey) {
        return load(userDetails, fileKey, false);
    }

    public DownloadableFile loadDownload(CustomUserDetails userDetails, String fileKey) {
        return load(userDetails, fileKey, true);
    }

    private DownloadableFile load(CustomUserDetails userDetails, String fileKey, boolean attachment) {
        FileKeySupport.ParsedFileKey parsedFileKey = FileKeySupport.parse(fileKey);
        return switch (parsedFileKey.fileType()) {
            case PORTFOLIO -> resolvePortfolioFile(userDetails, parsedFileKey.fileId(), attachment);
            case VERIFICATION -> resolveVerificationFile(userDetails, parsedFileKey.fileId(), attachment);
        };
    }

    private DownloadableFile resolvePortfolioFile(CustomUserDetails userDetails, Long fileId, boolean attachment) {
        FreelancerFile file = freelancerFileRepository.findDetailById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        Long currentUserId = currentUserIdOrNull(userDetails);
        boolean isAdmin = isAdmin(userDetails);
        boolean isOwner = currentUserId != null && file.isOwnedBy(currentUserId);
        boolean isPublicFile = file.getFreelancerProfile().isPublicProfile()
                && Boolean.TRUE.equals(file.getFreelancerProfile().getUser().getActiveYn());

        if (!isOwner && !isAdmin && !isPublicFile) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        return toDownloadableFile(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getFileUrl(),
                attachment
        );
    }

    private DownloadableFile resolveVerificationFile(CustomUserDetails userDetails, Long fileId, boolean attachment) {
        VerificationFile file = verificationFileRepository.findDetailById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        Long currentUserId = currentUserIdOrNull(userDetails);
        boolean isAdmin = isAdmin(userDetails);
        boolean isOwner = currentUserId != null && file.isOwnedBy(currentUserId);

        if (!isOwner && !isAdmin) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        return toDownloadableFile(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getFileUrl(),
                attachment
        );
    }

    private DownloadableFile toDownloadableFile(String originalFilename,
                                                String contentType,
                                                Long fileSize,
                                                String storagePath,
                                                boolean attachment) {
        Path path = fileStorageService.resolveReadablePath(storagePath);
        String resolvedContentType = contentType == null || contentType.isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : contentType;

        return new DownloadableFile(originalFilename, resolvedContentType, fileSize, path, attachment);
    }

    private Long currentUserIdOrNull(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUserId() : null;
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails != null
                && userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public record DownloadableFile(
            String originalFilename,
            String contentType,
            Long fileSize,
            Path path,
            boolean attachment
    ) {
    }
}
