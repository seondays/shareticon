package seondays.shareticon.group.dto;

import seondays.shareticon.userGroup.UserGroup;

public record GroupListResponse(Long groupId,
                                String groupTitleAlias) {
    public static GroupListResponse of(UserGroup userGroup) {
        return new GroupListResponse(
                userGroup.getGroup().getId(),
                userGroup.getGroupTitleAlias());
    }
}
