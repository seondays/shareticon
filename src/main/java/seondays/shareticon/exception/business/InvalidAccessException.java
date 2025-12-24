package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidAccessException extends BusinessException {

    private InvalidAccessException(Map<String, Object> context) {
        super("해당 그룹 및 쿠폰에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN, context);
    }

    public static InvalidAccessException of(Long groupId) {
        return new InvalidAccessException(Map.of("groupId", groupId));
    }
}
