package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class GroupUserNotFoundException extends BusinessException {

    private GroupUserNotFoundException(Map<String, Object> context) {
        super("해당 그룹에 참여중인 유저가 아닙니다", HttpStatus.NOT_FOUND, context);
    }

    public static GroupUserNotFoundException of(Long groupId) {
        return new GroupUserNotFoundException(Map.of("groupId", groupId));
    }
}
