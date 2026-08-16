package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class ImageDeleteException extends BusinessException {

    private final String objectKey;

    public ImageDeleteException(String objectKey) {
        super("S3 이미지 삭제 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
        this.objectKey = objectKey;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "objectKey", objectKey
        );
    }
}
