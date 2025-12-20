package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class VoucherNotFoundException extends BusinessException {

    public VoucherNotFoundException() {
        super("해당 쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
    }
}
