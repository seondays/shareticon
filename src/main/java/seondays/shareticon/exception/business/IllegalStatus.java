package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class IllegalStatus extends BusinessException {

    public IllegalStatus() {
        super("올바르지 않은 상태입니다", HttpStatus.BAD_REQUEST);
    }
}
