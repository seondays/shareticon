package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super("해당 유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND);
    }
}
