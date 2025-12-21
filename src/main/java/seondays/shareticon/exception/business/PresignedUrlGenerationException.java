package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class PresignedUrlGenerationException extends BusinessException {

    public PresignedUrlGenerationException() {
        super("쿠폰 이미지의 Presigned Url 생성 도중 문제가 발생했습니다. 다시 시도해주세요",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
