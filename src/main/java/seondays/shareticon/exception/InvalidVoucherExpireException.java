package seondays.shareticon.exception;

public class InvalidVoucherExpireException extends RuntimeException {

    public InvalidVoucherExpireException() {
        super("쿠폰 만료일은 오늘보다 이전 날짜로 지정할 수 없습니다");
    }
}