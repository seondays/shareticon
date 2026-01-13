package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("해당 유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND);
        this.userId = userId;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "userId", userId
        );
    }
}
