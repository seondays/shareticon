package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidAccessException extends BusinessException {

    private final Long groupId;

    public InvalidAccessException(Long groupId) {
        super("해당 그룹 및 쿠폰에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN);
        this.groupId = groupId;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "groupId", groupId
        );
    }
}
