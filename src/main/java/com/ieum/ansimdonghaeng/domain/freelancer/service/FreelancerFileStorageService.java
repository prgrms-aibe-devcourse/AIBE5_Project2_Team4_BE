package com.ieum.ansimdonghaeng.domain.freelancer.service;

import com.ieum.ansimdonghaeng.common.config.FileStorageProperties;
import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FreelancerFileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf",
            ".png",
            ".jpg",
            ".jpeg",
            ".txt"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "text/plain"
    );
    private static final Map<String, Set<String>> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "application/pdf", Set.of(".pdf"),
            "image/png", Set.of(".png"),
            "image/jpeg", Set.of(".jpg", ".jpeg"),
            "text/plain", Set.of(".txt")
    );

    private final Path baseDirectory;

    public FreelancerFileStorageService(FileStorageProperties fileStorageProperties) {
        this.baseDirectory = Path.of(fileStorageProperties.getBaseDir()).toAbsolutePath().normalize();
    }

    public StoredFile storePortfolio(Long freelancerProfileId, MultipartFile file) {
        return store(freelancerProfileId, "portfolio", file);
    }

    public StoredFile storeVerification(Long freelancerProfileId, Long verificationId, MultipartFile file) {
        return store(freelancerProfileId, "verifications/" + verificationId, file);
    }

    private StoredFile store(Long freelancerProfileId, String categoryPath, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "file is required.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()
        );
        String extension = extractExtension(originalFilename);
        validateFile(originalFilename, extension, file);

        String storedFilename = UUID.randomUUID() + extension;
        Path targetDirectory = baseDirectory
                .resolve("freelancers")
                .resolve(String.valueOf(freelancerProfileId))
                .resolve(categoryPath)
                .normalize();
        Path targetPath = targetDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(baseDirectory)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "invalid file path.");
        }

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetPath);
            return new StoredFile(originalFilename, storedFilename, targetPath.toString());
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    public void delete(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            return;
        }

        Path targetPath = Path.of(storagePath).toAbsolutePath().normalize();
        if (!targetPath.startsWith(baseDirectory)) {
            return;
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
            // Metadata deletion must not fail because a local file was already removed.
        }
    }

    private String extractExtension(String originalFilename) {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
    }

    private void validateFile(String originalFilename, String extension, MultipartFile file) {
        if (!StringUtils.hasText(originalFilename)
                || originalFilename.contains("..")
                || originalFilename.contains("/")
                || originalFilename.contains("\\")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "invalid file name.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.FILE_INVALID_TYPE);
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.FILE_INVALID_TYPE);
        }

        Set<String> allowedExtensions = EXTENSIONS_BY_CONTENT_TYPE.get(contentType);
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new CustomException(ErrorCode.FILE_INVALID_TYPE);
        }
    }

    public record StoredFile(String originalFilename, String storedFilename, String fileUrl) {
    }
}
