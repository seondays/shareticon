package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class AlreadyAppliedToGroupException extends BusinessException {

    private final String currentStatus;

    public AlreadyAppliedToGroupException(String currentStatus) {
        super("이미 그룹에 가입했거나 가입 신청 대기 중입니다", HttpStatus.BAD_REQUEST);
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
