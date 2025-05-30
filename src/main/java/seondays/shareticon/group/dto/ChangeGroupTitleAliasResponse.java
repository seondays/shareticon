package seondays.shareticon.group.dto;

import seondays.shareticon.userGroup.UserGroup;

public record ChangeGroupTitleAliasResponse(Long groupId,
                                            String titleAlias) {

    public static ChangeGroupTitleAliasResponse of(UserGroup userGroup) {
        return new ChangeGroupTitleAliasResponse(userGroup.getGroup().getId(),
                userGroup.getGroupTitleAlias());
    }
}
