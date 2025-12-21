package seondays.shareticon.exception;

import java.time.LocalDateTime;
import seondays.shareticon.exception.global.GlobalExceptionInfo;

public record CustomExceptionResponse(String message,
                                      LocalDateTime timestamp,
                                      int code) {

    public static CustomExceptionResponse of(GlobalExceptionInfo globalExceptionInfo) {
        LocalDateTime now = LocalDateTime.now();
        return new CustomExceptionResponse(globalExceptionInfo.getMessage(), now,
                globalExceptionInfo.getStatus().value());
    }

    public static CustomExceptionResponse of(String message, int statusCode) {
        LocalDateTime now = LocalDateTime.now();
        return new CustomExceptionResponse(message, now, statusCode);
    }
}
