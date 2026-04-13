package com.ieum.ansimdonghaeng.common.response;

import com.ieum.ansimdonghaeng.common.exception.ErrorCode;
import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        int status,
        LocalDateTime timestamp,
        String path
) {

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                errorCode.getCode(),
                message,
                errorCode.getStatus().value(),
                LocalDateTime.now(),
                path
        );
    }
}
