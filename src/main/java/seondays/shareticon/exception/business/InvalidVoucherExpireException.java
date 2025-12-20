package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class InvalidVoucherExpireException extends BusinessException {

    public InvalidVoucherExpireException() {
        super("쿠폰 만료일은 오늘보다 이전 날짜로 지정할 수 없습니다", HttpStatus.BAD_REQUEST);
    }
}