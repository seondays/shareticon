package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class InvalidJoinGroupException extends BusinessException {

    public InvalidJoinGroupException() {
        super("그룹 가입 대기중인 상태가 아닙니다", HttpStatus.BAD_REQUEST);
    }
}
