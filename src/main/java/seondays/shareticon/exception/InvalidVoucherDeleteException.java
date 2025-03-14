package seondays.shareticon.exception;

public class InvalidVoucherDeleteException extends RuntimeException {

    public InvalidVoucherDeleteException() {
        super("쿠폰 삭제가 불가능합니다");
    }
}
