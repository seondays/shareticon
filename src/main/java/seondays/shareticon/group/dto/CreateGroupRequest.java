package seondays.shareticon.group.dto;

import jakarta.validation.constraints.NotEmpty;

public record CreateGroupRequest(
        @NotEmpty(message = "그룹 이름을 포함해야 합니다")
        String title) {
}
