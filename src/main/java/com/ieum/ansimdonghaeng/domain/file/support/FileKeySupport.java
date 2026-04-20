package com.ieum.ansimdonghaeng.domain.file.support;

import com.ieum.ansimdonghaeng.common.exception.CustomException;
import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import org.springframework.util.StringUtils;

public final class FileKeySupport {

    private static final String PORTFOLIO_PREFIX = "portfolio";
    private static final String VERIFICATION_PREFIX = "verification";

    private FileKeySupport() {
    }

    public static String portfolioKey(Long fileId) {
        return PORTFOLIO_PREFIX + "-" + fileId;
    }

    public static String verificationKey(Long fileId) {
        return VERIFICATION_PREFIX + "-" + fileId;
    }

    public static String viewUrl(String fileKey) {
        return "/api/v1/files/" + fileKey;
    }

    public static String downloadUrl(String fileKey) {
        return viewUrl(fileKey) + "/download";
    }

    public static ParsedFileKey parse(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        int delimiterIndex = fileKey.indexOf('-');
        if (delimiterIndex <= 0 || delimiterIndex == fileKey.length() - 1) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String prefix = fileKey.substring(0, delimiterIndex);
        String rawId = fileKey.substring(delimiterIndex + 1);

        Long fileId;
        try {
            fileId = Long.parseLong(rawId);
        } catch (NumberFormatException exception) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        return switch (prefix) {
            case PORTFOLIO_PREFIX -> new ParsedFileKey(FileType.PORTFOLIO, fileId);
            case VERIFICATION_PREFIX -> new ParsedFileKey(FileType.VERIFICATION, fileId);
            default -> throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        };
    }

    public enum FileType {
        PORTFOLIO,
        VERIFICATION
    }

    public record ParsedFileKey(FileType fileType, Long fileId) {
    }
}
