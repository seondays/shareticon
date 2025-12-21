package seondays.shareticon.exception.business;

import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends BusinessException {

    public GroupNotFoundException() {
        super("해당 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
    }
}
