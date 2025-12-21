package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class AlreadyAppliedToGroupException extends BusinessException {

    public AlreadyAppliedToGroupException() {
        super("이미 그룹에 가입했거나 가입 신청 대기 중입니다", HttpStatus.BAD_REQUEST);
    }
}
