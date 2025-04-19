package seondays.shareticon.exception;

public class IllegalVoucherImageException extends RuntimeException {

    public IllegalVoucherImageException() {
        super("올바른 이미지 파일이 필요합니다");
    }
}
