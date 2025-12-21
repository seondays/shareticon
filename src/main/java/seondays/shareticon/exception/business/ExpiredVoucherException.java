package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class ExpiredVoucherException extends BusinessException {

    public ExpiredVoucherException() {
        super("만료된 쿠폰은 상태 변경이 불가능합니다", HttpStatus.BAD_REQUEST);
    }
}
