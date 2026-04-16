package com.ieum.ansimdonghaeng.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400", "Invalid request input."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "Authentication is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "Access is denied."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "Invalid or expired token."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "Requested resource was not found."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_404_1", "Project was not found."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_403_1", "You do not have access to this project."),
    PROJECT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "PROJECT_400_1", "Project status does not allow this action."),
    PROJECT_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "PROJECT_400_2", "requestedEndAt must be after requestedStartAt."),
    PROJECT_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "PROJECT_400_3",
            "At least one field must be provided for project update."),
    FREELANCER_NOT_FOUND(HttpStatus.NOT_FOUND, "FREELANCER_404_1", "Freelancer profile was not found."),
    FREELANCER_PROFILE_DUPLICATE(HttpStatus.CONFLICT, "FREELANCER_409_1", "Freelancer profile already exists."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "VERIFICATION_404_1", "Verification request was not found."),
    VERIFICATION_DUPLICATE(HttpStatus.CONFLICT, "VERIFICATION_409_1", "Pending verification request already exists."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_404_1", "File was not found."),
    FILE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "FILE_400_1", "File type is not allowed."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_400_2", "File size exceeds the allowed limit."),
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_500_1", "File storage failed."),
    PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "PROPOSAL_404_1", "Proposal was not found."),
    PROPOSAL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROPOSAL_403_1", "You do not have access to this proposal."),
    PROPOSAL_INVALID_STATUS(HttpStatus.BAD_REQUEST, "PROPOSAL_400_1", "Proposal status does not allow this action."),
    PROPOSAL_DUPLICATE(HttpStatus.CONFLICT, "PROPOSAL_409_1", "Proposal already exists for this freelancer."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_409", "Email is already registered."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401", "Invalid email or password."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_REFRESH", "Refresh token is invalid or expired."),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "AUTH_403", "User account is inactive."),
    OAUTH_EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "AUTH_400_OAUTH_EMAIL", "OAuth provider did not provide an email."),
    OAUTH_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "AUTH_502_OAUTH_PROVIDER", "OAuth provider request failed."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Unexpected server error.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
