package seondays.shareticon.exception.business;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends BusinessException {

    private final Long groupId;

    public GroupNotFoundException() {
        super("해당 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
        this.groupId = null;
    }

    public GroupNotFoundException(Long groupId) {
        super("해당 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
        this.groupId = groupId;
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("class", this.getClass().getSimpleName());
        if (groupId != null) {
            context.put("groupId", groupId);
        }
        return context;
    }
}
