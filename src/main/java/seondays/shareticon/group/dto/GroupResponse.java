package seondays.shareticon.group.dto;

import seondays.shareticon.group.Group;

public record GroupResponse(Long id,
                            String inviteCode) {

    public static GroupResponse of(Group group) {
        return new GroupResponse(group.getId(), group.getInviteCode());
    }
}
