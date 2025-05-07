package seondays.shareticon.group.dto;

import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;
import seondays.shareticon.userGroup.UserGroup;

public record ApplyToJoinResponse(Long applyUserId,
                                  String pendingUserName,
                                  Long targetGroupId) {

    public static ApplyToJoinResponse of(UserGroup userGroup) {
        User user = userGroup.getUser();
        Group group = userGroup.getGroup();
        return new ApplyToJoinResponse(user.getId(), user.getNickname(), group.getId());
    }
}
