package com.ieum.ansimdonghaeng.domain.freelancer.service;

import com.ieum.ansimdonghaeng.common.config.FileStorageProperties;
import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FreelancerFileStorageService {

    private static final String STORAGE_FILE_ROOT = "freelancers";
    private static final Set<String> LEGACY_STORAGE_BASE_NAMES = Set.of("local", "default");
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
        String storagePath = storagePath(freelancerProfileId, categoryPath, storedFilename);

        try {
            return new StoredFile(originalFilename, storedFilename, storagePath, file.getBytes());
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    public void delete(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            return;
        }

        Path targetPath = resolvePathOrNull(storagePath, false);
        if (targetPath == null) {
            return;
        }

        try {
            if (!Files.exists(targetPath)) {
                return;
            }
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
            // Metadata deletion must not fail because a local file was already removed.
        }
    }

    public Path resolveReadablePath(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        Path targetPath = resolvePathOrNull(storagePath, true);
        if (targetPath == null) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        return targetPath;
    }

    private String storagePath(Long freelancerProfileId, String categoryPath, String storedFilename) {
        return "freelancers/"
                + freelancerProfileId
                + "/"
                + categoryPath
                + "/"
                + storedFilename;
    }

    private Path resolvePathOrNull(String storagePath, boolean requireRegularFile) {
        List<Path> candidates = pathCandidates(storagePath);
        Set<Path> allowedBaseDirectories = allowedBaseDirectories();

        for (Path candidate : candidates) {
            Path normalizedCandidate = candidate.toAbsolutePath().normalize();
            if (allowedBaseDirectories.stream().noneMatch(normalizedCandidate::startsWith)) {
                continue;
            }
            if (!requireRegularFile || Files.isRegularFile(normalizedCandidate)) {
                return normalizedCandidate;
            }
        }

        return null;
    }

    private List<Path> pathCandidates(String storagePath) {
        List<Path> candidates = new ArrayList<>();
        try {
            Path rawPath = Path.of(storagePath);
            if (rawPath.isAbsolute()) {
                candidates.add(rawPath);
            } else {
                candidates.add(baseDirectory.resolve(rawPath));
            }
        } catch (InvalidPathException ignored) {
            // Legacy values may contain another OS' absolute path syntax. Try extracting the storage suffix below.
        }

        extractStorageRelativePath(storagePath).ifPresent(relativePath -> {
            candidates.add(baseDirectory.resolve(relativePath));
            Path storageRoot = baseDirectory.getParent();
            if (storageRoot != null) {
                for (String legacyBaseName : LEGACY_STORAGE_BASE_NAMES) {
                    candidates.add(storageRoot.resolve(legacyBaseName).resolve(relativePath));
                }
            }
        });

        return candidates;
    }

    private Optional<Path> extractStorageRelativePath(String storagePath) {
        String normalized = storagePath.replace('\\', '/');
        String marker = "/" + STORAGE_FILE_ROOT + "/";
        int markerIndex = normalized.lastIndexOf(marker);
        String relativeValue;
        if (markerIndex >= 0) {
            relativeValue = normalized.substring(markerIndex + 1);
        } else if (normalized.startsWith(STORAGE_FILE_ROOT + "/")) {
            relativeValue = normalized;
        } else {
            return Optional.empty();
        }

        try {
            Path relativePath = Path.of(relativeValue).normalize();
            if (relativePath.isAbsolute()
                    || relativePath.startsWith("..")
                    || !relativePath.startsWith(STORAGE_FILE_ROOT)) {
                return Optional.empty();
            }
            return Optional.of(relativePath);
        } catch (InvalidPathException exception) {
            return Optional.empty();
        }
    }

    private Set<Path> allowedBaseDirectories() {
        Set<Path> allowedDirectories = new HashSet<>();
        allowedDirectories.add(baseDirectory);

        Path storageRoot = baseDirectory.getParent();
        if (storageRoot != null) {
            for (String legacyBaseName : LEGACY_STORAGE_BASE_NAMES) {
                allowedDirectories.add(storageRoot.resolve(legacyBaseName).toAbsolutePath().normalize());
            }
        }

        return allowedDirectories;
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

    public record StoredFile(String originalFilename, String storedFilename, String fileUrl, byte[] fileData) {
    }
}
