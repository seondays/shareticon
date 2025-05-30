package seondays.shareticon.group.dto;

import jakarta.validation.constraints.NotEmpty;

public record ChangeGroupTitleAliasRequest(
        @NotEmpty(message = "변경할 이름을 포함해야 합니다")
        String newGroupTitleAlias
) {

}
