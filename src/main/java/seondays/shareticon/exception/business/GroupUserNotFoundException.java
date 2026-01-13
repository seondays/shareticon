package seondays.shareticon.exception.business;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class GroupUserNotFoundException extends BusinessException {

    private final Long groupId;

    public GroupUserNotFoundException(Long groupId) {
        super("해당 그룹에 참여중인 유저가 아닙니다", HttpStatus.NOT_FOUND);
        this.groupId = groupId;
    }

    @Override
    public Map<String, Object> getContext() {
        return Map.of(
                "class", this.getClass().getSimpleName(),
                "groupId", groupId
        );
    }
}
