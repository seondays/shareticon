package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidGroupLeaderException extends BusinessException {

    private InvalidGroupLeaderException(Map<String, Object> context) {
        super("그룹 관리 작업의 권한이 없습니다. 해당 그룹의 리더 유저만 가능합니다", HttpStatus.FORBIDDEN, context);
    }

    public static InvalidGroupLeaderException of(Long groupId) {
        return new InvalidGroupLeaderException(Map.of("groupId", groupId));
    }
}
