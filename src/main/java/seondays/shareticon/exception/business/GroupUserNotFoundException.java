package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class GroupUserNotFoundException extends BusinessException {

    public GroupUserNotFoundException() {
        super("해당 그룹과 유저에 대한 정보가 존재하지 않습니다", HttpStatus.NOT_FOUND);
    }
}
