package seondays.shareticon.exception;

public class InvalidAccessVoucherException extends RuntimeException {

    public InvalidAccessVoucherException() {
        super("쿠폰을 조회할 권한이 없습니다");
    }
}
