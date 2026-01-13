package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class GroupCreateException extends BusinessException {

    public GroupCreateException() {
        super("그룹 생성 중 오류가 발생했습니다. 다시 시도해주세요", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of("class", this.getClass().getSimpleName());
    }
}
