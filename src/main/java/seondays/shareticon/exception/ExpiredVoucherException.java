package seondays.shareticon.exception;

public class ExpiredVoucherException extends RuntimeException {

    public ExpiredVoucherException() {
        super("만료된 쿠폰은 상태 변경이 불가능합니다.");
    }
}
