package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends BusinessException {

    private GroupNotFoundException(Map<String, Object> context) {
        super("해당 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND, context);
    }

    public static GroupNotFoundException of(Long groupId) {
        return new GroupNotFoundException(Map.of("groupId", groupId));
    }

    public static GroupNotFoundException ofInviteCode(String inviteCode) {
        return new GroupNotFoundException(Map.of("inviteCode", inviteCode));
    }
}
