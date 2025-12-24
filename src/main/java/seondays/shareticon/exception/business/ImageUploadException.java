package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ImageUploadException extends BusinessException {

    private ImageUploadException(Map<String, Object> context) {
        super("S3 이미지 업로드 중 오류가 발생했습니다. 다시 시도해주세요", HttpStatus.BAD_REQUEST, context);
    }

    public static ImageUploadException of(String imageKey) {
        return new ImageUploadException(Map.of("imageKey", imageKey));
    }
}
