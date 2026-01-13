package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class PresignedUrlGenerationException extends BusinessException {

    private final String imageKey;

    public PresignedUrlGenerationException(String imageKey) {
        super("쿠폰 이미지의 Presigned Url 생성 도중 문제가 발생했습니다. 다시 시도해주세요",
                HttpStatus.INTERNAL_SERVER_ERROR);
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
