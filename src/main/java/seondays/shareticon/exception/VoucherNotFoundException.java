package seondays.shareticon.exception;

public class VoucherNotFoundException extends RuntimeException {

    public VoucherNotFoundException() {
        super("해당 쿠폰을 찾을 수 없습니다");
    }
}
