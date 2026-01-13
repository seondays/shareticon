package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class InvalidJoinGroupException extends BusinessException {

    private final String currentStatus;

    public InvalidJoinGroupException(String currentStatus) {
        super("그룹 가입 대기중인 상태가 아닙니다", HttpStatus.BAD_REQUEST);
        this.currentStatus = currentStatus;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "currentStatus", currentStatus
        );
    }
}
