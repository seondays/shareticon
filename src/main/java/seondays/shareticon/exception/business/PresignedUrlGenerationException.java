package seondays.shareticon.exception.business;

public class PresignedUrlGenerationException extends RuntimeException {

    public PresignedUrlGenerationException() {
        super("쿠폰 이미지의 Presigned Url 생성 도중 문제가 발생했습니다. 다시 시도해주세요");
    }
}
