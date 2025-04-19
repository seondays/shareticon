package seondays.shareticon.exception.handler;

import java.time.LocalDateTime;

public record CustomExceptionResponse(String message,
                                      LocalDateTime timestamp,
                                      int code) {
    public static CustomExceptionResponse of(String message, int code) {
        LocalDateTime now = LocalDateTime.now();
        return new CustomExceptionResponse(message, now, code);
    }
}
