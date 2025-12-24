package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class IllegalStatusException extends BusinessException {

    public IllegalStatusException() {
        super("올바르지 않은 상태입니다", HttpStatus.BAD_REQUEST);
    }
}
