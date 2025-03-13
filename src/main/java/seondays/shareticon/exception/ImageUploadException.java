package seondays.shareticon.exception;

public class ImageUploadException extends RuntimeException {

    public ImageUploadException() {
        super("S3 이미지 업로드가 실패하였습니다");
    }
}
