package seondays.shareticon.exception;

public class NoVoucherImageException extends RuntimeException {

    public NoVoucherImageException() {
        super("쿠폰 이미지가 포함되어야 합니다");
    }
}
