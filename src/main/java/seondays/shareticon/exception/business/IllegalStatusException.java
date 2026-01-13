package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class IllegalStatusException extends BusinessException {

    public IllegalStatusException() {
        super("올바르지 않은 상태입니다", HttpStatus.BAD_REQUEST);
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of("class", this.getClass().getSimpleName());
    }
}
