package seondays.shareticon.exception;

public class InvalidAccessException extends RuntimeException {

    public InvalidAccessException() {
        super("해당 그룹 및 쿠폰에 접근할 권한이 없습니다");
    }
}
