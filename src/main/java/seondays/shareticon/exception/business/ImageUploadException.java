package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ImageUploadException extends BusinessException {

    private final String imageKey;

    public ImageUploadException(String imageKey) {
        super("S3 이미지 업로드 중 오류가 발생했습니다. 다시 시도해주세요", HttpStatus.BAD_REQUEST);
        this.imageKey = imageKey;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "imageKey", imageKey
        );
    }
}
