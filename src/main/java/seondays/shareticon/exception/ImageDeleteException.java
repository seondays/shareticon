package seondays.shareticon.exception;

public class ImageDeleteException extends RuntimeException {

    public ImageDeleteException() {
        super("쿠폰 이미지 삭제가 실패했습니다");
    }
}
