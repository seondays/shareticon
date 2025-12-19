package seondays.shareticon.exception.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalExceptionInfo {
    private final String message;
    private final HttpStatus status;

    public GlobalExceptionInfo(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public static GlobalExceptionInfo of(String message, HttpStatus status) {
        return new GlobalExceptionInfo(message, status);
    }
}
