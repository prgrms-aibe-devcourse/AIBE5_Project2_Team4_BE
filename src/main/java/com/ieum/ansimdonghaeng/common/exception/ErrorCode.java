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
