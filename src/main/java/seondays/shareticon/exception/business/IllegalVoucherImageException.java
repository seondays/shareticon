package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class IllegalVoucherImageException extends BusinessException {

    public IllegalVoucherImageException() {
        super("올바른 이미지 파일이 필요합니다", HttpStatus.BAD_REQUEST);
    }
}
