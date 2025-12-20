package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class InvalidVoucherDeleteException extends BusinessException {

    public InvalidVoucherDeleteException() {
        super("쿠폰을 등록한 유저만 쿠폰 삭제가 가능합니다", HttpStatus.FORBIDDEN);
    }
}
