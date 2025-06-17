package seondays.shareticon.exception;

public class PresignedUrlGenerationException extends RuntimeException {

    public PresignedUrlGenerationException() {
        super("쿠폰 이미지의 Presigned Url 생성 도중 문제가 발생했습니다");
    }
}
