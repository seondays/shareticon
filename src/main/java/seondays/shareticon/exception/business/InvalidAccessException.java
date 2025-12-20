package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class InvalidAccessException extends BusinessException {

    public InvalidAccessException() {
        super("해당 그룹 및 쿠폰에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN);
    }
}
