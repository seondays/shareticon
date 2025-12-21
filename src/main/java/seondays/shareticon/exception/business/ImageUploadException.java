package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class ImageUploadException extends BusinessException {

    public ImageUploadException() {
        super("S3 이미지 업로드 중 오류가 발생했습니다. 다시 시도해주세요", HttpStatus.BAD_REQUEST);
    }
}
